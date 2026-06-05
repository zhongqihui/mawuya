/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.niudeyapi.mawuya.core.enums.ResultCodeEnum;

/**
 * 全站统一公共响应体（遵循《阿里巴巴 Java 开发手册》接口返回值规约）。
 *
 * <p>结构：</p>
 * <pre>
 *   {
 *     "code":    "00000",          // ResultCodeEnum.code（5 位字符串：成功 00000，错误 A/B/C 段）
 *     "message": "成功",            // 人类可读
 *     "data":    {...} | (省略)     // 业务数据；为 null 时通过 NON_NULL 序列化省略字段
 *   }
 * </pre>
 *
 * <p>所有标了 {@code @RestController} 且未被 {@code @SkipApiResponseWrap} 排除的 JSON 接口，
 * 由 {@code ApiResponseAdvice} 自动包装；业务代码直接 return 业务对象 / DTO 即可。</p>
 *
 * @author 钟启辉
 * @param <T> data 数据载体类型
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
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.data = data;
    }

    // ============================================================
    // 工厂：成功
    // ============================================================

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, null);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, data);
    }

    /** 自定义 message 的成功（如「已通过」「已删除」） */
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    // ============================================================
    // 工厂：失败
    // ============================================================

    public static <T> BaseResponse<T> error(ResultCodeEnum codeEnum) {
        return new BaseResponse<>(codeEnum, null);
    }

    public static <T> BaseResponse<T> error(ResultCodeEnum codeEnum, String message) {
        return new BaseResponse<>(codeEnum.getCode(),
                message != null && !message.isEmpty() ? message : codeEnum.getMessage(),
                null);
    }

    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    /** 系统级未知异常 */
    public static <T> BaseResponse<T> systemError() {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERROR, null);
    }

    public static <T> BaseResponse<T> systemError(String message) {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERROR.getCode(),
                message != null && !message.isEmpty()
                        ? message
                        : ResultCodeEnum.SYSTEM_ERROR.getMessage(),
                null);
    }

    // ============================================================
    // 判定
    // ============================================================

    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.getCode().equals(this.code);
    }

    // ============================================================
    // getter / setter
    // ============================================================

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

    @Override
    public String toString() {
        return "BaseResponse{code='" + code + "', message='" + message + "', data=" + data + '}';
    }
}
