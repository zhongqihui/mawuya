/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.controller;

import com.niudeyapi.mawuya.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * AMS 自定义错误页 controller。
 *
 * <p>替换 Spring Boot 默认的 {@code Whitelabel Error Page}：
 * <ul>
 *   <li>{@code 4xx → /pub/404}（已有美化模板）</li>
 *   <li>{@code 5xx 与其他 → /pub/500}（已有动画 + 渐变样式模板）</li>
 *   <li>统一隐藏底层异常堆栈、SQL、文件路径，仅向用户暴露友好提示与故障时间</li>
 *   <li>{@code Accept: application/json} 请求改返回 JSON {code,message}，便于前端 AJAX 调用</li>
 * </ul>
 *
 * <p>所有原始异常会以 ERROR 级别打入服务端日志（含完整 stacktrace），便于事后排查。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends AbstractErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);

    /** 视图模板：404 / 500 */
    private static final String VIEW_404 = "pub/404";
    private static final String VIEW_500 = "pub/500";

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @Override
    public String getErrorPath() {
        // Spring Boot 2.x 中本方法已被废弃；以 server.error.path 注入路径为准。
        return "/error";
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String errorHtml(HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = resolveStatus(request);
        // 落日志：用 RequestDispatcher 属性还原原始 URI 与异常
        logServerSideError(request, status);

        // 对用户友好的提示挂到 model；500 模板会读 errTitle/errDesc 增强可定制性
        model.addAttribute("errTitle", "服务器开小差了");
        model.addAttribute("errDesc",  "我们正在抢修，请稍后再试。");

        response.setStatus(status.value());
        // 4xx 走 404 页（含 404/405/403 等都对最终用户一个体验）
        if (status.is4xxClientError()) {
            return VIEW_404;
        }
        return VIEW_500;
    }

    /**
     * 兜底 JSON 输出（对 AJAX / 接口客户端）。
     * 仅对外暴露 code/message/timestamp，绝不返回堆栈或 SQL。
     */
    @RequestMapping
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> errorJson(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = resolveStatus(request);
        logServerSideError(request, status);
        response.setStatus(status.value());

        Map<String, Object> body = getErrorAttributes(request, /*includeStackTrace*/ false);
        // 强制清掉可能泄漏的字段（exception/trace/errors/message）
        body.remove("exception");
        body.remove("trace");
        body.remove("errors");
        body.put("status",  status.value());
        body.put("message", status.is4xxClientError() ? "请求的资源不存在或无法处理" : "服务器开小差了，请稍后再试");
        body.put("code",    status.is4xxClientError() ? "A0404" : "B0500");
        return body;
    }

    /** 把 BusinessException 视为「展示给用户」的可控异常，仍按 500 模板渲染但 message 显式化 */
    @SuppressWarnings("unused")
    public static void noteBusinessException(BusinessException e) {
        // 占位：业务异常一律由 GlobalWebExceptionHandler 优先处理；
        // 走到 ErrorController 说明上游没拦住，落入兜底分支。
        log.debug("[errors] fallthrough business: code={}, msg={}", e.getCode(), e.getMessage());
    }

    // ============================ helpers ============================

    private HttpStatus resolveStatus(HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (code instanceof Integer) {
                status = HttpStatus.valueOf((Integer) code);
            }
        } catch (Exception ignored) {
            // 未识别状态码：保持 500
        }
        return status;
    }

    private void logServerSideError(HttpServletRequest request, HttpStatus status) {
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        // 4xx 不打 ERROR（避免日志噪声），仅 5xx 打 ERROR
        if (status.is5xxServerError()) {
            if (exObj instanceof Throwable) {
                log.error("[ams-error] {} {} -> {}", request.getMethod(), uri, status,
                        (Throwable) exObj);
            } else {
                log.error("[ams-error] {} {} -> {}", request.getMethod(), uri, status);
            }
        } else if (log.isInfoEnabled()) {
            log.info("[ams-error] {} {} -> {}", request.getMethod(), uri, status);
        }
    }
}
