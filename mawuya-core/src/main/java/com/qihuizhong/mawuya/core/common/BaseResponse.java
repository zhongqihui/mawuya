/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qihuizhong.mawuya.core.enums.ResultCodeEnum;

/**
 * 统一公共响应体。
 *
 * <p>结构：</p>
 * <pre>
 *   {
 *     "code":    "000000",      // ResultCodeEnum.resultCode
 *     "message": "成功",         // 人类可读
 *     "data":    {...} | null   // 业务数据；空时省略（NON_NULL 序列化）
 *   }
 * </pre>
 *
 * <p>所有标了 {@code @RestController} 或 {@code @ResponseBody} 且未被
 * {@code @SkipApiResponseWrap} 排除的 JSON 接口，由 {@code ApiResponseAdvice}
 * 自动包装，业务代码直接 return 业务对象 / DTO 即可。</p>
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    /** 响应码（参见 {@link ResultCodeEnum}） */
    private String code;

    /** 提示信息 */
    private String message;

    /** 返回数据（NON_NULL：null 时不参与序列化） */
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(ResultCodeEnum codeEnum, T data) {
        this.code = codeEnum.getResultCode();
        this.message = codeEnum.getResultMessage();
        this.data = data;
    }

    // ---------------- 工厂：成功 ----------------

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, null);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, data);
    }

    /** 自定义 message 的成功（如"已通过"/"已删除"） */
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS.getResultCode(), message, data);
    }

    // ---------------- 工厂：失败 ----------------

    public static <T> BaseResponse<T> error(ResultCodeEnum codeEnum) {
        return new BaseResponse<>(codeEnum, null);
    }

    public static <T> BaseResponse<T> error(ResultCodeEnum codeEnum, String message) {
        return new BaseResponse<>(codeEnum.getResultCode(),
                message != null && !message.isEmpty() ? message : codeEnum.getResultMessage(),
                null);
    }

    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    /** 系统级未知异常 */
    public static <T> BaseResponse<T> systemError() {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERR, null);
    }

    public static <T> BaseResponse<T> systemError(String message) {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERR.getResultCode(),
                message != null && !message.isEmpty() ? message : ResultCodeEnum.SYSTEM_ERR.getResultMessage(),
                null);
    }

    // ---------------- 判定 ----------------

    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.getResultCode().equals(this.code);
    }

    // ---------------- getter / setter ----------------

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
