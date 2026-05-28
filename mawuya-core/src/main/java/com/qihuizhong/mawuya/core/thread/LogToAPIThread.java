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
 * 调用第三方 IP 接口（淘宝），解析后丢入 DB 队列。
 * <p>仅当 {@code mawuya.async.enabled=true} 时启用（默认在 ams 模块启用）。</p>
 *
 * @author zqh
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class LogToAPIThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LogToAPIThread.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
                    taobaoSend(logInfo);
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
     * 调用淘宝 IP 接口（已下线则会失败，失败后由回调回退入库）
     */
    private void taobaoSend(LogInfo info) {
        if (StringUtils.isEmpty(info.getIpAddr())) {
            try {
                DataCenter.getLogInfoToDBQueue().put(info);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        String url = "http://ip.taobao.com/service/getIpInfo.php?ip=" + info.getIpAddr();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/4.0")
                .addHeader("Accept", "application/json")
                .get()
                .build();
        OkHttpUtil.enqueue(request, new TaobaoCallback(info));
    }

    /**
     * 淘宝 IP 回调：成功填充地理信息后入库；失败 3 次后直接入库。
     */
    private static class TaobaoCallback implements Callback {
        private final LogInfo logInfo;

        TaobaoCallback(LogInfo logInfo) {
            this.logInfo = logInfo == null ? new LogInfo() : logInfo;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            try {
                if (logInfo.getTryTimes() >= 3) {
                    DataCenter.getLogInfoToDBQueue().put(logInfo);
                } else {
                    logInfo.setTryTimes(logInfo.getTryTimes() + 1);
                    DataCenter.getLogInfoToAPIQueue().put(logInfo);
                    if (log.isWarnEnabled()) {
                        log.warn("taobao ip api failed, ip={}, retry={}", logInfo.getIpAddr(), logInfo.getTryTimes());
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    String text = body.string();
                    JsonNode root = MAPPER.readTree(text);
                    JsonNode codeNode = root.get("code");
                    if (codeNode != null && "0".equals(codeNode.asText())) {
                        JsonNode data = root.get("data");
                        if (data != null) {
                            logInfo.setCountry(textOf(data, "country"))
                                    .setProvince(textOf(data, "region"))
                                    .setCity(textOf(data, "city"))
                                    .setIsp(textOf(data, "isp"));
                        }
                    }
                }
                DataCenter.getLogInfoToDBQueue().put(logInfo);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("parse taobao response failed: {}", e.getMessage());
                }
                try {
                    DataCenter.getLogInfoToDBQueue().put(logInfo);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static String textOf(JsonNode node, String field) {
            JsonNode v = node.get(field);
            return v == null ? null : v.asText();
        }
    }
}
