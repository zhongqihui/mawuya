/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.interceptor;

import com.niudeyapi.mawuya.common.utils.DateUtil;
import com.niudeyapi.mawuya.core.cache.DataCenter;
import com.niudeyapi.mawuya.core.dataobject.LogDO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * 访客日志收集拦截器
 *
 * @author zqh
 */
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);

    /**
     * 请求头总长度上限（字节级保护）。
     * <p>DB 列是 TEXT（65535），实际正常浏览器单次请求 header 几 KB 即顶；
     * 设 8KB 兜底，杜绝恶意请求灌爆字段或拖慢入库。</p>
     */
    private static final int MAX_HEADERS_LEN = 8 * 1024;

    /**
     * 需要掩码的敏感请求头（不区分大小写）。
     * <p>这些 header 在 log 表里会以 {@code ***masked***} 写入，避免管理后台
     * 日志详情页明文展示 Cookie / Bearer token，杜绝越权获取访客身份。</p>
     */
    private static final Set<String> MASKED_HEADERS = buildMaskedHeaderSet();

    private static Set<String> buildMaskedHeaderSet() {
        Set<String> s = new HashSet<>();
        s.add("cookie");
        s.add("set-cookie");
        s.add("authorization");
        s.add("proxy-authorization");
        return s;
    }

    // ============================================================
    // 健康检查 / 探活类请求识别（不写入访客日志）
    // ------------------------------------------------------------
    // 远端容器 / PaaS（Docker、k8s liveness、ELB、Nginx upstream check）
    // 普遍会从「环回地址」用 curl / kube-probe / Go-http-client / Apache-HttpClient
    // 等 UA 频繁请求 `/` 或常见探活路径。这些请求被原样写库会让 PV/UV 失真，
    // 访客列表也会被 127.0.0.1 刷屏（线上观察 2.7w 条全是 127.0.0.1+curl/7.81.0）。
    //
    // 判定策略（保守，命中任一即跳过）：
    //   1) 客户端 IP 是环回地址 AND UA 是健康检查家族 → 探活探针，必跳过
    //   2) UA 是健康检查家族 AND 路径是常见探活路径   → 探活探针，必跳过
    //   3) 路径是 `/healthz` / `/actuator/health` / `/ping` 等专用探活端点
    //      → 无论 IP / UA 都跳过
    // 留出 system property `mawuya.log.skip-probe=false` 关闭整套策略的逃生口。
    // ============================================================

    /** 系统属性：可强制关闭探活过滤（仅排障时使用）。默认 true=过滤。 */
    private static final boolean SKIP_PROBE_ENABLED =
            !"false".equalsIgnoreCase(System.getProperty("mawuya.log.skip-probe", "true"));

    /** 环回地址集合（IPv4 + IPv6 + ::1 全写） */
    private static final Set<String> LOOPBACK_IPS = buildLoopbackIps();

    private static Set<String> buildLoopbackIps() {
        Set<String> s = new HashSet<>();
        s.add("127.0.0.1");
        s.add("::1");
        s.add("0:0:0:0:0:0:0:1");
        s.add("localhost");
        return s;
    }

    /**
     * 健康检查家族 UA 关键字（小写匹配，前缀/包含均算命中）。
     * <p>这些 UA 在公网真实访客中几乎不会出现；即使出现也是爬虫/脚本，
     * 计入 PV/UV 的价值很低。</p>
     */
    private static final String[] PROBE_UA_KEYWORDS = new String[]{
            "curl/",                  // curl 探活、运维脚本
            "wget/",                  // wget 抓取
            "kube-probe",             // k8s liveness / readiness
            "go-http-client",         // Go 内置 client，常见于 sidecar probe
            "apache-httpclient",      // jmeter / 自动化探活
            "java/",                  // 部分 JDK 内置 client
            "okhttp/",                // 移动端 / 中间层探活
            "python-requests",        // python 探活脚本
            "elb-healthchecker",      // AWS ELB
            "googlehc",               // GCP LB
            "alibaba-loadbalancer",   // 阿里云 SLB
            "kong/",                  // Kong upstream check
            "consul",                 // consul health check
            "node-fetch"              // node 探活
    };

    /**
     * 显式探活路径（不区分客户端 IP/UA 都跳过）。
     * <p>这些路径项目本身并不提供，但反代/PaaS 会主动构造，写库纯粹噪声。</p>
     */
    private static final Set<String> PROBE_PATHS = buildProbePaths();

    private static Set<String> buildProbePaths() {
        Set<String> s = new HashSet<>();
        s.add("/healthz");
        s.add("/health");
        s.add("/readyz");
        s.add("/livez");
        s.add("/ping");
        s.add("/actuator/health");
        s.add("/actuator/info");
        return s;
    }

    /**
     * 判断一次请求是否属于「健康检查/探活」家族，应跳过访客日志记录。
     *
     * <p>独立成方法便于单元测试与日后调整规则。任何异常都按"不跳过"处理，
     * 避免日志收集逻辑因 NPE 反而把请求记成「失败」。</p>
     */
    private boolean isProbeRequest(HttpServletRequest request, String ipAddr) {
        if (!SKIP_PROBE_ENABLED) {
            return false;
        }
        try {
            String uri = request.getRequestURI();
            if (uri != null && PROBE_PATHS.contains(uri)) {
                return true;
            }
            String ua = request.getHeader("User-Agent");
            String uaLc = ua == null ? "" : ua.toLowerCase();
            boolean uaIsProbe = false;
            if (!uaLc.isEmpty()) {
                for (String kw : PROBE_UA_KEYWORDS) {
                    if (uaLc.contains(kw)) {
                        uaIsProbe = true;
                        break;
                    }
                }
            }
            // UA 是探活家族 + 来源是环回地址 → 一定是容器/PaaS 内部探活
            if (uaIsProbe && LOOPBACK_IPS.contains(ipAddr)) {
                return true;
            }
            // UA 是探活家族 + 路径是根/静态：90% 概率也是反代探活
            if (uaIsProbe && uri != null && ("/".equals(uri) || "/favicon.ico".equals(uri))) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 早期跳过健康检查：不创建 LogDO、不挂到 request attribute，
        // afterCompletion 拿不到 attribute 自然就不会入队列。
        String ipAddr = getIpAddr(request);
        if (isProbeRequest(request, ipAddr)) {
            return true;
        }

        // 注意：tryTimes 显式置 0。LogDO.tryTimes 已改为包装类型 Integer，
        // 不显式设值会导致 LogToAPIThread.handleAllFailed 在自动拆箱时 NPE。
        LogDO logInfo = new LogDO()
                .setIpAddr(ipAddr)
                .setBrowser(getOsAndBrowserInfo(request))
                .setReqHeaders(getReqHeaders(request))
                .setReqTime(String.valueOf(System.currentTimeMillis()))
                .setParams(getParams(request))
                .setReqMethod(request.getMethod())
                .setReqUrl(request.getRequestURL().toString())
                .setTryTimes(0);

        request.setAttribute("logInfo", logInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        Object attr = request.getAttribute("logInfo");
        if (!(attr instanceof LogDO)) {
            return;
        }
        LogDO logInfo = (LogDO) attr;
        long start = 0L;
        try {
            start = Long.parseLong(logInfo.getReqTime());
        } catch (NumberFormatException ignored) {
            // ignore
        }
        long now = System.currentTimeMillis();
        logInfo.setRespStatus("0")
                .setExceptMessage("success")
                .setConsumeTime((now - start) + "ms")
                .setReqTime(DateUtil.long2Str(start))
                .setRespTime(DateUtil.long2Str(now));

        if (e != null) {
            logInfo.setRespStatus("1").setExceptMessage(e.getMessage());
        }

        try {
            // PV/UV 主路径：直接入 DB 队列，保证访客日志必然落库——不依赖外部 geo 接口可用性；
            // 这样即使 ip-api.com 网络不通，PV/UV 数据也不会丢。
            //
            // 历史实现是「先入 API 队列查 geo → 成功才入 DB」，导致 ip-api 接口阻断时
            // 整条 PV 都丢；线上表现就是「PV/UV 半天才动一下」甚至「完全不增加」。
            BlockingQueue<LogDO> dbQ = DataCenter.getLogInfoToDBQueue();
            if (dbQ.remainingCapacity() > 0) {
                dbQ.put(logInfo);
            } else if (log.isWarnEnabled()) {
                log.warn("logInfoToDBQueue is full, drop one record. ip={}", logInfo.getIpAddr());
            }

            // geo 异步补全：把同一引用塞进 API 队列，LogToAPIThread 回调成功后会
            // 直接改写该对象的 country/province/city/isp 字段。
            // 由于 LogToDBThread 与 LogToAPIThread 是两条独立流水线，存在竞态：
            //   - 若 LogToDBThread 先 insert，geo 字段是 null（接受，dashboard 不依赖 geo）；
            //   - 若 geo 先到，LogToDBThread 入库时携带 geo 信息（最佳情况）。
            // 这里使用 offer 非阻塞，队列满直接放弃 geo 补全（不影响 PV/UV 入库）。
            DataCenter.getLogInfoToAPIQueue().offer(logInfo);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 把请求头序列化为多行 {@code Name: value} 字符串。
     *
     * <p>处理细节：</p>
     * <ul>
     *   <li>同名 header 多值用 {@code , } 连接（HTTP 语义允许重复 header）；</li>
     *   <li>敏感 header（{@link #MASKED_HEADERS}）整体以 {@code ***masked***} 写入，
     *       避免管理后台明文展示 Cookie / Bearer token；</li>
     *   <li>总长度兜底截断到 {@link #MAX_HEADERS_LEN}，附带 {@code ...truncated} 标记，
     *       防止恶意巨型 header 撑爆 DB 字段；</li>
     *   <li>遇任何异常都返回 null（不阻塞主流程），仅在 debug 日志中记录。</li>
     * </ul>
     */
    private String getReqHeaders(HttpServletRequest request) {
        try {
            Enumeration<String> names = request.getHeaderNames();
            if (names == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder(256);
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name == null) {
                    continue;
                }
                sb.append(name).append(": ");
                if (MASKED_HEADERS.contains(name.toLowerCase())) {
                    sb.append("***masked***");
                } else {
                    Enumeration<String> values = request.getHeaders(name);
                    boolean first = true;
                    while (values != null && values.hasMoreElements()) {
                        if (!first) {
                            sb.append(", ");
                        }
                        sb.append(values.nextElement());
                        first = false;
                    }
                }
                sb.append('\n');
                // 早退：避免拼接超大 header 后再做一次截断的内存开销
                if (sb.length() > MAX_HEADERS_LEN) {
                    break;
                }
            }
            if (sb.length() == 0) {
                return null;
            }
            if (sb.length() > MAX_HEADERS_LEN) {
                return sb.substring(0, MAX_HEADERS_LEN) + "\n...truncated";
            }
            return sb.toString();
        } catch (Exception ex) {
            // 日志收集不应该影响业务主流程：任何异常都吞掉，仅留 debug 痕迹
            if (log.isDebugEnabled()) {
                log.debug("[log] collect headers failed: {}", ex.getMessage());
            }
            return null;
        }
    }

    private String getParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String next = names.nextElement();
            sb.append(next)
                    .append(": ")
                    .append(Arrays.toString(request.getParameterValues(next)))
                    .append(";");
        }
        return sb.toString();
    }

    private String getIpAddr(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        String xFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(xFor) && !"unKnown".equalsIgnoreCase(xFor)) {
            int index = xFor.indexOf(',');
            if (index != -1) {
                return xFor.substring(0, index);
            }
            return xFor;
        }

        xFor = xRealIp;
        if (StringUtils.isNotEmpty(xFor) && !"unKnown".equalsIgnoreCase(xFor)) {
            return xFor;
        }
        if (StringUtils.isBlank(xFor) || "unknown".equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(xFor) || "unknown".equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(xFor) || "unknown".equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(xFor) || "unknown".equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(xFor) || "unknown".equalsIgnoreCase(xFor)) {
            xFor = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(xFor) ? "127.0.0.1" : xFor;
    }

    public static String getOsAndBrowserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown,Unknown";
        }
        String user = userAgent.toLowerCase();

        String os;
        String browser;

        if (user.contains("windows")) {
            os = "Windows";
        } else if (user.contains("mac")) {
            os = "Mac";
        } else if (user.contains("x11")) {
            os = "Unix";
        } else if (user.contains("android")) {
            os = "Android";
        } else if (user.contains("iphone")) {
            os = "IPhone";
        } else {
            // 未识别系统：仅记录 UA 前 60 字符，避免拼接整段 UA 撑爆字段
            os = "Unknown-" + truncate(userAgent, 60);
        }

        try {
            if (user.contains("edge")) {
                browser = (userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("msie")) {
                String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
                browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
            } else if (user.contains("safari") && user.contains("version")) {
                browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                        + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
            } else if (user.contains("opr") || user.contains("opera")) {
                if (user.contains("opera")) {
                    browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                            + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
                } else {
                    browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                            .replace("OPR", "Opera");
                }
            } else if (user.contains("chrome")) {
                browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("firefox")) {
                browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("rv")) {
                String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
                browser = "IE" + IEVersion.substring(0, IEVersion.length() - 1);
            } else {
                // 未识别浏览器：仅记录 UA 前 60 字符
                browser = "Unknown-" + truncate(userAgent, 60);
            }
        } catch (Exception ex) {
            browser = "Unknown-" + truncate(userAgent, 60);
        }

        // 最终防御：log_info.browser 字段限长（DDL 已扩到 500），仍兜底 480 字符截断
        // 留 20 字符余量给"," + 极端 fallback 场景，杜绝任何写入失败
        return truncate(os + "," + browser, 480);
    }

    /**
     * 安全截断字符串到指定长度，保留前缀。null/短串原样返回。
     * 用于 UA 等不可控外部输入，避免破坏 SQL 字段长度约束。
     */
    private static String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen);
    }
}
