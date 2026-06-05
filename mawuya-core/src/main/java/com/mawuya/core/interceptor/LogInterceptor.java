/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.interceptor;

import com.mawuya.common.utils.DateUtil;
import com.mawuya.core.cache.DataCenter;
import com.mawuya.core.dataobject.LogDO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * 访客日志收集拦截器
 *
 * @author zqh
 */
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 注意：tryTimes 显式置 0。LogDO.tryTimes 已改为包装类型 Integer，
        // 不显式设值会导致 LogToAPIThread.handleAllFailed 在自动拆箱时 NPE。
        LogDO logInfo = new LogDO()
                .setIpAddr(getIpAddr(request))
                .setBrowser(getOsAndBrowserInfo(request))
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
            if (DataCenter.getLogInfoToAPIQueue().remainingCapacity() > 0) {
                DataCenter.getLogInfoToAPIQueue().put(logInfo);
            } else if (log.isWarnEnabled()) {
                log.warn("logInfoToAPIQueue is full, drop one record. ip={}", logInfo.getIpAddr());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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
            os = "UnKnown, More-Info: " + userAgent;
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
                browser = "UnKnown, More-Info: " + userAgent;
            }
        } catch (Exception ex) {
            browser = "UnKnown, More-Info: " + userAgent;
        }

        return os + "," + browser;
    }
}
