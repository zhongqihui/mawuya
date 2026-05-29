/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.enums;

/**
 * 全站统一结果码枚举。
 *
 * <p>编码规则：6 位字符串。前 3 位 = 模块（000=公共/通用），后 3 位 = 业务码。
 * 用字符串而非数字 —— 便于跨语言/跨系统传递，且未来对外 OpenAPI 也无歧义。</p>
 *
 * <p>分类：</p>
 * <ul>
 *   <li>{@code 000xxx} 通用：成功、参数校验、未知错误</li>
 *   <li>{@code 001xxx} 鉴权 / 授权</li>
 *   <li>{@code 002xxx} 资源（NotFound / Conflict）</li>
 *   <li>{@code 003xxx} 业务规则</li>
 *   <li>{@code 004xxx} 上传 / 文件</li>
 *   <li>{@code 999xxx} 系统级故障</li>
 * </ul>
 *
 * @author 钟启辉
 */
public enum ResultCodeEnum {

    // ---------------- 通用 ----------------
    SUCCESS(            "000000", "成功"),
    PARAM_INVALID(      "000400", "参数不合法"),
    PARAM_MISSING(      "000401", "缺少必填参数"),

    // ---------------- 鉴权 ----------------
    UNAUTHORIZED(       "001401", "未登录或登录已过期"),
    FORBIDDEN(          "001403", "无权访问"),
    LOGIN_FAILED(       "001410", "用户名或密码错误"),
    ACCOUNT_DISABLED(   "001411", "账号已禁用"),

    // ---------------- 资源 ----------------
    NOT_FOUND(          "002404", "资源不存在"),
    CONFLICT(           "002409", "资源已存在或冲突"),

    // ---------------- 业务 ----------------
    BUSINESS_ERROR(     "003000", "业务处理失败"),

    // ---------------- 上传 ----------------
    UPLOAD_INVALID(     "004400", "文件不合法"),
    UPLOAD_TOO_LARGE(   "004413", "文件超出大小限制"),
    UPLOAD_FAILED(      "004500", "文件上传失败"),

    // ---------------- 系统 ----------------
    SYSTEM_ERR(         "999500", "系统异常，请联系 zhongqihui996@gmail.com"),
    ;

    private final String resultCode;
    private final String resultMessage;

    ResultCodeEnum(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
