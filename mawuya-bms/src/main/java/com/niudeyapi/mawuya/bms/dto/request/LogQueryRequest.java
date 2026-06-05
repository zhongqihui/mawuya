/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import com.niudeyapi.mawuya.core.dto.request.PageRequest;

/**
 * 访问日志多条件查询请求 DTO（{@code GET /bms/api/log/page}）。
 *
 * @author 钟启辉
 */
public class LogQueryRequest extends PageRequest {

    private String ipAddr;
    private String reqUrl;
    private String reqMethod;
    private String respStatus;
    private String country;
    private String province;
    /** 起始时间，格式 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd */
    private String startTime;
    private String endTime;
    /** 排序字段（白名单：sn / ip_addr / req_method / resp_status / consume_time / req_time） */
    private String orderField;
    /** 排序方向 ASC / DESC */
    private String orderDir;

    public String getIpAddr() { return ipAddr; }
    public LogQueryRequest setIpAddr(String ipAddr) { this.ipAddr = ipAddr; return this; }

    public String getReqUrl() { return reqUrl; }
    public LogQueryRequest setReqUrl(String reqUrl) { this.reqUrl = reqUrl; return this; }

    public String getReqMethod() { return reqMethod; }
    public LogQueryRequest setReqMethod(String reqMethod) { this.reqMethod = reqMethod; return this; }

    public String getRespStatus() { return respStatus; }
    public LogQueryRequest setRespStatus(String respStatus) { this.respStatus = respStatus; return this; }

    public String getCountry() { return country; }
    public LogQueryRequest setCountry(String country) { this.country = country; return this; }

    public String getProvince() { return province; }
    public LogQueryRequest setProvince(String province) { this.province = province; return this; }

    public String getStartTime() { return startTime; }
    public LogQueryRequest setStartTime(String startTime) { this.startTime = startTime; return this; }

    public String getEndTime() { return endTime; }
    public LogQueryRequest setEndTime(String endTime) { this.endTime = endTime; return this; }

    public String getOrderField() { return orderField; }
    public LogQueryRequest setOrderField(String orderField) { this.orderField = orderField; return this; }

    public String getOrderDir() { return orderDir; }
    public LogQueryRequest setOrderDir(String orderDir) { this.orderDir = orderDir; return this; }
}
