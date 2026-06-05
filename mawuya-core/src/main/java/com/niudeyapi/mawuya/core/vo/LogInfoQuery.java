/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.vo;

import java.io.Serializable;

/**
 * 访客日志多条件查询 DTO（mawuya-bms 日志管理模块使用）。
 *
 * <p>所有字段皆可选；空字符串/null 视为不过滤。时间范围作用于 {@code req_time}
 * （表中字段是 VARCHAR(50)，但写入时是 yyyy-MM-dd HH:mm:ss 字符串，词法序与时序一致，
 * 因此可直接做字符串比较）。</p>
 *
 * <p>排序字段与方向由 service 层做白名单校验，xml 不做字符串拼接以外的事。</p>
 */
public class LogInfoQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** IP 模糊匹配（前缀 + 部分） */
    private String ipAddr;
    /** 请求路径模糊匹配 */
    private String reqUrl;
    /** 请求方法精确匹配（GET/POST/...） */
    private String reqMethod;
    /** 响应状态精确匹配（"0"=成功 / "1"=失败） */
    private String respStatus;
    /** 国家精确匹配 */
    private String country;
    /** 省份模糊匹配 */
    private String province;
    /** 时间范围下界（含）：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd */
    private String startTime;
    /** 时间范围上界（含）：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd */
    private String endTime;

    /** 排序字段（已通过 service 白名单收敛后填入） */
    private String orderField;
    /** 排序方向 ASC/DESC（已通过 service 白名单收敛后填入） */
    private String orderDir;

    /** 分页：偏移量 */
    private Integer offset;
    /** 分页：每页条数 */
    private Integer limit;

    public String getIpAddr() { return ipAddr; }
    public LogInfoQuery setIpAddr(String ipAddr) { this.ipAddr = ipAddr; return this; }

    public String getReqUrl() { return reqUrl; }
    public LogInfoQuery setReqUrl(String reqUrl) { this.reqUrl = reqUrl; return this; }

    public String getReqMethod() { return reqMethod; }
    public LogInfoQuery setReqMethod(String reqMethod) { this.reqMethod = reqMethod; return this; }

    public String getRespStatus() { return respStatus; }
    public LogInfoQuery setRespStatus(String respStatus) { this.respStatus = respStatus; return this; }

    public String getCountry() { return country; }
    public LogInfoQuery setCountry(String country) { this.country = country; return this; }

    public String getProvince() { return province; }
    public LogInfoQuery setProvince(String province) { this.province = province; return this; }

    public String getStartTime() { return startTime; }
    public LogInfoQuery setStartTime(String startTime) { this.startTime = startTime; return this; }

    public String getEndTime() { return endTime; }
    public LogInfoQuery setEndTime(String endTime) { this.endTime = endTime; return this; }

    public String getOrderField() { return orderField; }
    public LogInfoQuery setOrderField(String orderField) { this.orderField = orderField; return this; }

    public String getOrderDir() { return orderDir; }
    public LogInfoQuery setOrderDir(String orderDir) { this.orderDir = orderDir; return this; }

    public Integer getOffset() { return offset; }
    public LogInfoQuery setOffset(Integer offset) { this.offset = offset; return this; }

    public Integer getLimit() { return limit; }
    public LogInfoQuery setLimit(Integer limit) { this.limit = limit; return this; }
}
