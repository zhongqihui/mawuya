/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.enums;

/**
 * 全站统一结果码枚举（遵循《阿里巴巴 Java 开发手册（嵩山版）》错误码规约）。
 *
 * <p>编码规则：5 位字符串。首字母 + 4 位数字。</p>
 * <ul>
 *   <li><b>A</b> 段：用户端错误（参数 / 鉴权 / 资源 / 业务）</li>
 *   <li><b>B</b> 段：当前系统执行错误（系统超时 / 系统异常）</li>
 *   <li><b>C</b> 段：第三方服务错误（中间件 / RPC / 数据库 / 文件存储）</li>
 * </ul>
 *
 * <p>设计要点：错误码大类区分（先 A/B/C 定位责任方），二级宏观码区分（41xx 客户端鉴权类、43xx 资源类...），
 * 末位再分细。所有码值同时回填到响应体 {@code BaseResponse.code} 与日志，便于跨服务排查。</p>
 *
 * @author 钟启辉
 */
public enum ResultCodeEnum {

    // ============== 通用成功 ==============
    /** 一切正常 */
    SUCCESS(            "00000", "成功"),

    // ============== A 段：用户端错误（A0xxx ~ A4xxx） ==============
    /** A0001：用户端错误（兜底） */
    USER_ERROR(         "A0001", "用户端错误"),

    // ----- A01xx 注册 / A02xx 登录 / A03xx 校验码 / A04xx 用户校验 -----
    /** A0210：用户名或密码错误 */
    LOGIN_FAILED(       "A0210", "用户名或密码错误"),
    /** A0220：账号已禁用 */
    ACCOUNT_DISABLED(   "A0220", "账号已禁用"),
    /** A0230：未登录或登录已过期 */
    UNAUTHORIZED(       "A0230", "未登录或登录已过期"),
    /** A0301：访问未授权 */
    FORBIDDEN(          "A0301", "无权访问"),

    // ----- A04xx 参数校验 -----
    /** A0400：用户请求参数错误（兜底） */
    PARAM_INVALID(      "A0400", "参数不合法"),
    /** A0410：必填参数缺失 */
    PARAM_MISSING(      "A0410", "缺少必填参数"),
    /** A0421：参数类型错误 */
    PARAM_TYPE_ERROR(   "A0421", "参数类型不正确"),
    /** A0430：请求体格式错误 */
    PARAM_FORMAT_ERROR( "A0430", "请求体格式错误"),
    /** A0440：HTTP 方法不支持 */
    METHOD_NOT_ALLOWED( "A0440", "不支持的 HTTP 方法"),

    // ----- A05xx 资源 / A06xx 业务规则 -----
    /** A0500：资源不存在 */
    NOT_FOUND(          "A0500", "资源不存在"),
    /** A0501：资源已存在 / 冲突 */
    CONFLICT(           "A0501", "资源已存在或冲突"),
    /** A0600：业务规则失败（兜底） */
    BUSINESS_ERROR(     "A0600", "业务处理失败"),

    // ----- A07xx 文件上传 -----
    /** A0700：文件不合法 */
    UPLOAD_INVALID(     "A0700", "文件不合法"),
    /** A0701：文件大小超限 */
    UPLOAD_TOO_LARGE(   "A0701", "文件超出大小限制"),
    /** A0702：文件上传失败 */
    UPLOAD_FAILED(      "A0702", "文件上传失败"),

    // ============== B 段：系统执行错误（B0xxx） ==============
    /** B0001：系统执行出错（兜底） */
    SYSTEM_ERROR(       "B0001", "系统繁忙，请稍后再试"),
    /** B0100：系统执行超时 */
    SYSTEM_TIMEOUT(     "B0100", "系统执行超时"),

    // ============== C 段：第三方服务错误（C0xxx） ==============
    /** C0001：调用第三方服务出错（兜底） */
    THIRD_SERVICE_ERROR("C0001", "依赖服务异常"),
    /** C0111：数据库错误 */
    DB_ERROR(           "C0111", "数据库异常"),
    /** C0211：文件存储错误 */
    STORAGE_ERROR(      "C0211", "文件存储异常"),
    ;

    /** 错误码（5 位字符串：A0001 / B0001 / C0001 / 00000） */
    private final String code;

    /** 默认中文提示，可被业务层覆盖 */
    private final String message;

    ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    // ---------------- 兼容旧字段名（getResultCode / getResultMessage） ----------------
    // 渐进迁移期保留：调用方一律推荐使用 getCode() / getMessage()。

    /** @deprecated 请使用 {@link #getCode()} */
    @Deprecated
    public String getResultCode() {
        return code;
    }

    /** @deprecated 请使用 {@link #getMessage()} */
    @Deprecated
    public String getResultMessage() {
        return message;
    }
}
