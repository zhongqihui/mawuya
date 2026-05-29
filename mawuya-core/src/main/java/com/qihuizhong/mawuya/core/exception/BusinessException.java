/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.exception;

import com.qihuizhong.mawuya.core.enums.ResultCodeEnum;

/**
 * 业务异常：携带 {@link ResultCodeEnum} 与可定制的提示信息。
 *
 * <p>用法：</p>
 * <pre>
 *   throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "sn 不能为空");
 *   throw new BusinessException("用户名已存在");      // 默认 BUSINESS_ERROR 码
 * </pre>
 *
 * <p>由 {@code GlobalExceptionHandler} 捕获并统一返回 {@code BaseResponse}，
 * 业务代码无需自己 try-catch 拼响应。</p>
 *
 * @author 钟启辉
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public BusinessException(ResultCodeEnum codeEnum) {
        super(codeEnum.getResultMessage());
        this.code = codeEnum.getResultCode();
    }

    public BusinessException(ResultCodeEnum codeEnum, String message) {
        super(message != null && !message.isEmpty() ? message : codeEnum.getResultMessage());
        this.code = codeEnum.getResultCode();
    }

    /** 不指定具体码，默认走 BUSINESS_ERROR */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.BUSINESS_ERROR.getResultCode();
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
