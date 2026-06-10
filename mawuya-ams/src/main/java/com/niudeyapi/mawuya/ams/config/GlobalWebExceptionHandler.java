/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.config;

import com.niudeyapi.mawuya.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AMS 全局异常拦截。
 *
 * <p>职责（与 {@code CustomErrorController} 协同）：</p>
 * <ul>
 *   <li>业务异常 / Spring MVC 异常 / 任意未受检异常 → 统一渲染友好错误页或 JSON；</li>
 *   <li>禁止 SQL / 堆栈 / 类名 等任何底层细节透传到用户；</li>
 *   <li>原始异常仍以 ERROR 级别打入服务端日志，便于排查。</li>
 * </ul>
 *
 * <p>BusinessException 等"业务可控异常"按 200 + 提示 message 返回；
 * 其余按 500 渲染错误页（HTML）或 JSON。</p>
 *
 * @author 钟启辉
 */
@ControllerAdvice(basePackages = "com.niudeyapi.mawuya.ams.controller")
public class GlobalWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

    /** 视图模板：500 错误页 */
    private static final String VIEW_500 = "pub/500";
    /** 视图模板：404 错误页 */
    private static final String VIEW_404 = "pub/404";

    /**
     * 业务异常：用户已经看到的可预期错误（如「文章不存在」），通常按 200 + 提示渲染。
     * <p>HTML 场景仍渲染 500 页面但 errDesc 直显业务信息；JSON 场景返回 code+message。</p>
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException ex, HttpServletRequest req, HttpServletResponse resp) {
        if (log.isInfoEnabled()) {
            log.info("[ams-biz] {} {} -> {}/{}", req.getMethod(), req.getRequestURI(), ex.getCode(), ex.getMessage());
        }
        if (isJsonRequest(req)) {
            resp.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(jsonBody(ex.getCode(), safeBizMessage(ex.getMessage())));
        }
        ModelAndView mv = new ModelAndView(VIEW_500);
        mv.addObject("errTitle", "操作未完成");
        mv.addObject("errDesc",  safeBizMessage(ex.getMessage()));
        // 业务异常不算服务器错误，给浏览器 200 让 Thymeleaf 正常渲染（不触发浏览器自己的 500 兜底）
        resp.setStatus(HttpStatus.OK.value());
        return mv;
    }

    /**
     * 兜底：任意未受检异常 → 友好 500 页 / 通用 JSON。
     * <p>不暴露异常类名、message、SQL、堆栈中的任意一行；
     * 服务端按 ERROR 级别留全栈日志便于事后排查。</p>
     */
    @ExceptionHandler(Throwable.class)
    public Object handleAny(Throwable ex, HttpServletRequest req, HttpServletResponse resp) {
        log.error("[ams-error] {} {} -> 500 [{}]",
                req.getMethod(), req.getRequestURI(), ex.getClass().getSimpleName(), ex);

        if (isJsonRequest(req)) {
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(jsonBody("B0500", "服务器开小差了，请稍后再试"));
        }
        ModelAndView mv = new ModelAndView(VIEW_500);
        mv.addObject("errTitle", "服务器开小差了");
        mv.addObject("errDesc",  "我们正在抢修，请稍后再试。");
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return mv;
    }

    // ============================== helpers ==============================

    /** 判断是否为 JSON 请求（XHR / Accept: application/json / 路径含 /api/） */
    private boolean isJsonRequest(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) {
            return true;
        }
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        String uri = req.getRequestURI();
        return uri != null && uri.contains("/api/");
    }

    /** 业务异常 message 兜底：null / 空时给通用提示，避免 errDesc 渲染成 null */
    private String safeBizMessage(String message) {
        return (message == null || message.isEmpty()) ? "操作未能完成，请稍后再试" : message;
    }

    private Map<String, Object> jsonBody(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>(4);
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        return body;
    }
}
