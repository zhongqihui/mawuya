/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.exception;

import com.niudeyapi.mawuya.core.enums.ResultCodeEnum;

/**
 * 业务异常：携带 {@link ResultCodeEnum} 与可定制的提示信息。
 *
 * <p>用法：</p>
 * <pre>
 *   throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "sn 不能为空");
 *   throw new BusinessException("用户名已存在");      // 默认 BUSINESS_ERROR 码（A0600）
 * </pre>
 *
 * <p>由 {@code GlobalExceptionHandler} 捕获并统一返回 {@code BaseResponse}，
 * 业务代码无需自己 try-catch 拼响应体。</p>
 *
 * @author 钟启辉
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码（5 位字符串：A0001 / B0001 / C0001 …） */
    private final String code;

    public BusinessException(ResultCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
    }

    public BusinessException(ResultCodeEnum codeEnum, String message) {
        super(message != null && !message.isEmpty() ? message : codeEnum.getMessage());
        this.code = codeEnum.getCode();
    }

    /** 不指定具体码，默认走 BUSINESS_ERROR */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.BUSINESS_ERROR.getCode();
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "BusinessException{code='" + code + "', message='" + getMessage() + "'}";
    }
}
