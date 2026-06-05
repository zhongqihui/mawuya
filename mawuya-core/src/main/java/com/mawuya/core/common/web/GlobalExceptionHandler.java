/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.common.web;

import com.mawuya.core.common.BaseResponse;
import com.mawuya.core.enums.ResultCodeEnum;
import com.mawuya.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器（位于 core 模块，被 ams / bms 共享）。
 *
 * <p>把所有从 controller 抛出的异常统一转换为 {@link BaseResponse}，
 * 业务代码无需写 try-catch / 手工拼 {@code success=0} 样板。</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>只处理与 spring-web / mvc / javax.validation 直接相关的异常</li>
 *   <li>Spring Security 相关异常由 bms 模块的 {@code BmsSecurityExceptionHandler} 处理（解耦 core 不依赖 security）</li>
 *   <li>basePackages 限定到 {@code com.mawuya}，不影响第三方</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============== 业务自定义异常 ==============

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Void> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.info("[business] {} {} -> code={} msg={}", req.getMethod(), req.getRequestURI(),
                ex.getCode(), ex.getMessage());
        return BaseResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<Void> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        log.info("[illegal-arg] {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return BaseResponse.error(ResultCodeEnum.PARAM_INVALID, ex.getMessage());
    }

    // ============== 参数校验 ==============

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("参数不合法");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_INVALID, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数不合法");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_INVALID, msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_MISSING,
                        "缺少必填参数: " + ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_INVALID,
                        "参数类型不正确: " + ex.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_INVALID,
                        "请求体格式错误"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(BaseResponse.error(ResultCodeEnum.PARAM_INVALID,
                        "不支持的 HTTP 方法: " + ex.getMethod()));
    }

    // ============== 上传 ==============

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(BaseResponse.error(ResultCodeEnum.UPLOAD_TOO_LARGE,
                        "上传文件超出大小限制"));
    }

    // ============== 兜底 ==============

    @ExceptionHandler(Throwable.class)
    public BaseResponse<Void> handleAny(Throwable ex, HttpServletRequest req) {
        log.error("[unhandled] {} {}", req.getMethod(), req.getRequestURI(), ex);
        return BaseResponse.systemError(ex.getMessage());
    }
}
