/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 用户访问信息实体（与 log_info 表对应）
 *
 * @author zqh
 */
public class LogInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private String ipAddr;
    private String country;
    private String province;
    private String city;
    private String area;
    private String detailPosition;
    private String isp;
    /** 根据 ip 接口查询失败次数，3 次之后不再请求 */
    private int tryTimes;
    private String reqTime;
    private String respTime;
    private String consumeTime;
    private String reqUrl;
    private String params;
    private String reqMethod;
    private String browser;
    /** 响应状态：0 成功；1 失败 */
    private String respStatus;
    private String exceptMessage;

    public LogInfo() {
    }

    public Integer getSn() {
        return sn;
    }

    public LogInfo setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public LogInfo setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public LogInfo setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public LogInfo setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCity() {
        return city;
    }

    public LogInfo setCity(String city) {
        this.city = city;
        return this;
    }

    public String getArea() {
        return area;
    }

    public LogInfo setArea(String area) {
        this.area = area;
        return this;
    }

    public String getDetailPosition() {
        return detailPosition;
    }

    public LogInfo setDetailPosition(String detailPosition) {
        this.detailPosition = detailPosition;
        return this;
    }

    public String getIsp() {
        return isp;
    }

    public LogInfo setIsp(String isp) {
        this.isp = isp;
        return this;
    }

    public int getTryTimes() {
        return tryTimes;
    }

    public LogInfo setTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
        return this;
    }

    public String getReqTime() {
        return reqTime;
    }

    public LogInfo setReqTime(String reqTime) {
        this.reqTime = reqTime;
        return this;
    }

    public String getRespTime() {
        return respTime;
    }

    public LogInfo setRespTime(String respTime) {
        this.respTime = respTime;
        return this;
    }

    public String getConsumeTime() {
        return consumeTime;
    }

    public LogInfo setConsumeTime(String consumeTime) {
        this.consumeTime = consumeTime;
        return this;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public LogInfo setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
        return this;
    }

    public String getParams() {
        return params;
    }

    public LogInfo setParams(String params) {
        this.params = params;
        return this;
    }

    public String getReqMethod() {
        return reqMethod;
    }

    public LogInfo setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
        return this;
    }

    public String getBrowser() {
        return browser;
    }

    public LogInfo setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    public String getRespStatus() {
        return respStatus;
    }

    public LogInfo setRespStatus(String respStatus) {
        this.respStatus = respStatus;
        return this;
    }

    public String getExceptMessage() {
        return exceptMessage;
    }

    public LogInfo setExceptMessage(String exceptMessage) {
        this.exceptMessage = exceptMessage;
        return this;
    }
}
