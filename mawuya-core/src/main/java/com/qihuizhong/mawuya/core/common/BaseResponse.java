/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2020 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.common;

import com.qihuizhong.mawuya.core.enums.ResultCodeEnum;

/**
 * author: zhongqihui996@gmail.com
 * date: 2020/9/15
 * description: 公共的响应体，返回前端及第三方
 */
public class BaseResponse<T> {

    /**
     * 响应码
     */
    private String code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(ResultCodeEnum resultCodeEnum, T data) {
        this.code = resultCodeEnum.getResultCode();
        this.message = resultCodeEnum.getResultMessage();
        this.data = data;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, data);
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(ResultCodeEnum.SUCCESS, null);
    }

    public static <T> BaseResponse fail() {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERR, null);
    }

    private static <T> BaseResponse<T> fail(T data) {
        return new BaseResponse<>(ResultCodeEnum.SYSTEM_ERR, data);
    }

    private static <T> BaseResponse<T> fail(ResultCodeEnum resultCodeEnum, T data) {
        return new BaseResponse<>(resultCodeEnum, data);
    }

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
