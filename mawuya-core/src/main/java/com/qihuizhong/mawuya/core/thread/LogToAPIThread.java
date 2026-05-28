/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qihuizhong.mawuya.common.utils.OkHttpUtil;
import com.qihuizhong.mawuya.core.cache.DataCenter;
import com.qihuizhong.mawuya.core.entity.LogInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 调用第三方 IP 接口，解析后丢入 DB 队列。
 *
 * <p>2026-05 起淘宝接口（ip.taobao.com/service/getIpInfo.php）已下线返回 404，
 * 改为以下双源策略：</p>
 * <ol>
 *   <li><b>主源</b> ip-api.com：免费、无需 KEY、原生 lang=zh-CN，
 *       45 次/分钟/出口 IP，对个人博客足够。
 *       URL: {@code http://ip-api.com/json/{ip}?lang=zh-CN&fields=status,message,country,regionName,city,isp,query}</li>
 *   <li><b>兜底</b> ipapi.co（英文）：当 ip-api 网络失败或返回 status!=success 时回退。
 *       URL: {@code https://ipapi.co/{ip}/json/}</li>
 *   <li><b>最终兜底</b>：双源都失败 + 重试 3 次 → 无地理信息直接入库，不阻塞日志主线。</li>
 * </ol>
 *
 * <p>仅当 {@code mawuya.async.enabled=true} 时启用（默认在 ams 模块启用）。</p>
 *
 * @author zqh
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class LogToAPIThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LogToAPIThread.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 主源 ip-api.com：免费 + 中文 + 无 KEY；fields 收敛只取需要字段，减少响应体 */
    private static final String IPAPI_PRIMARY =
            "http://ip-api.com/json/%s?lang=zh-CN&fields=status,message,country,regionName,city,isp,query";

    /** 兜底源 ipapi.co：英文，但全球可用；偶尔返回 429 时再交给重试 */
    private static final String IPAPI_FALLBACK = "https://ipapi.co/%s/json/";

    /** 失败重试上限（与原行为一致） */
    private static final int MAX_TRY = 3;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public void shutDown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            if (!DataCenter.getLogInfoToAPIQueue().isEmpty()) {
                LogInfo logInfo = DataCenter.getLogInfoToAPIQueue().poll();
                if (logInfo != null) {
                    queryGeo(logInfo);
                }
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 入口：先查主源，主源失败时由 callback 自动 fallback；都失败则回退入队/入库。
     */
    private void queryGeo(LogInfo info) {
        if (StringUtils.isEmpty(info.getIpAddr())) {
            // 没拿到访客 IP 直接入库，不浪费一次外呼
            enqueueDb(info);
            return;
        }
        callPrimary(info);
    }

    /** 调用主源 ip-api.com */
    private void callPrimary(LogInfo info) {
        String url = String.format(IPAPI_PRIMARY, info.getIpAddr());
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (mawuya-blog log)")
                .addHeader("Accept", "application/json")
                .get()
                .build();
        OkHttpUtil.enqueue(request, new GeoCallback(info, /*isFallback*/ false));
    }

    /** 调用兜底源 ipapi.co */
    private void callFallback(LogInfo info) {
        String url = String.format(IPAPI_FALLBACK, info.getIpAddr());
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (mawuya-blog log)")
                .addHeader("Accept", "application/json")
                .get()
                .build();
        OkHttpUtil.enqueue(request, new GeoCallback(info, /*isFallback*/ true));
    }

    /** 把 logInfo 推入 DB 入库队列；中断异常正确传播。 */
    private static void enqueueDb(LogInfo info) {
        try {
            DataCenter.getLogInfoToDBQueue().put(info);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 通用回调：根据 isFallback 决定是用兜底解析路径还是主源解析路径。
     * 主源失败 → 自动尝试兜底；兜底也失败 → 走重试或最终入库。
     */
    private class GeoCallback implements Callback {
        private final LogInfo logInfo;
        private final boolean isFallback;

        GeoCallback(LogInfo logInfo, boolean isFallback) {
            this.logInfo = logInfo == null ? new LogInfo() : logInfo;
            this.isFallback = isFallback;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            // 网络层失败：主源失败立即尝试兜底；兜底也失败再走重试。
            if (!isFallback) {
                if (log.isDebugEnabled()) {
                    log.debug("ip-api primary network failed, try fallback. ip={}, err={}",
                            logInfo.getIpAddr(), e.getMessage());
                }
                callFallback(logInfo);
                return;
            }
            handleAllFailed("network: " + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String text = null;
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    text = body.string();
                }
            }

            // HTTP 状态码非 2xx：主源失败转兜底；兜底失败走重试
            if (!response.isSuccessful() || StringUtils.isEmpty(text)) {
                if (!isFallback) {
                    callFallback(logInfo);
                } else {
                    handleAllFailed("http " + response.code());
                }
                return;
            }

            try {
                JsonNode root = MAPPER.readTree(text);
                boolean ok = isFallback ? parseIpapiCo(root, logInfo) : parseIpApiCom(root, logInfo);
                if (ok) {
                    enqueueDb(logInfo);
                } else if (!isFallback) {
                    // 主源响应可解析但 status!=success（比如保留地址/限流）→ 转兜底
                    callFallback(logInfo);
                } else {
                    // 兜底也无可用数据 → 重试或入库
                    handleAllFailed("both api returned no usable data");
                }
            } catch (Exception parseEx) {
                if (log.isDebugEnabled()) {
                    log.debug("parse {} response failed: {}", isFallback ? "ipapi.co" : "ip-api.com",
                            parseEx.getMessage());
                }
                if (!isFallback) {
                    callFallback(logInfo);
                } else {
                    handleAllFailed("parse: " + parseEx.getMessage());
                }
            }
        }

        /** 双源都失败时的统一处理：未到重试上限就重新入主源队列；否则入库。 */
        private void handleAllFailed(String reason) {
            if (logInfo.getTryTimes() >= MAX_TRY) {
                if (log.isWarnEnabled()) {
                    log.warn("geo lookup gave up after {} tries, ip={}, lastReason={}",
                            logInfo.getTryTimes(), logInfo.getIpAddr(), reason);
                }
                enqueueDb(logInfo);
                return;
            }
            logInfo.setTryTimes(logInfo.getTryTimes() + 1);
            try {
                DataCenter.getLogInfoToAPIQueue().put(logInfo);
                if (log.isDebugEnabled()) {
                    log.debug("geo lookup retry, ip={}, retry={}, reason={}",
                            logInfo.getIpAddr(), logInfo.getTryTimes(), reason);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ---------------- 解析器 ----------------

    /**
     * 解析 ip-api.com 响应。
     * <pre>
     * 成功：{"status":"success","country":"中国","regionName":"广东省","city":"深圳","isp":"电信","query":"x.x.x.x"}
     * 失败：{"status":"fail","message":"private range","query":"127.0.0.1"}
     * </pre>
     * @return true 表示成功提取了地理信息；false 表示需要走兜底。
     */
    private static boolean parseIpApiCom(JsonNode root, LogInfo info) {
        JsonNode status = root.get("status");
        if (status == null || !"success".equals(status.asText())) {
            return false;
        }
        info.setCountry(textOf(root, "country"))
                .setProvince(textOf(root, "regionName"))
                .setCity(textOf(root, "city"))
                .setIsp(textOf(root, "isp"));
        return true;
    }

    /**
     * 解析 ipapi.co 响应。
     * <pre>
     * 成功：{"ip":"x.x.x.x","country_name":"China","region":"Guangdong","city":"Shenzhen","org":"Chinanet"}
     * 失败：{"error":true,"reason":"RateLimited"}
     * </pre>
     */
    private static boolean parseIpapiCo(JsonNode root, LogInfo info) {
        JsonNode err = root.get("error");
        if (err != null && err.asBoolean(false)) {
            return false;
        }
        // 至少要有 country_name 才算成功
        String country = textOf(root, "country_name");
        if (StringUtils.isEmpty(country)) {
            return false;
        }
        info.setCountry(country)
                .setProvince(textOf(root, "region"))
                .setCity(textOf(root, "city"))
                .setIsp(textOf(root, "org"));
        return true;
    }

    private static String textOf(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
