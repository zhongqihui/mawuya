/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 用户访问日志数据对象（与数据库表 {@code log_info} 对应）。
 *
 * <p>{@code toString()} 不输出全量字段，仅保留排查关键的 IP / URL / 状态 / 耗时，
 * 避免日志爆炸。</p>
 *
 * @author 钟启辉
 */
public class LogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志主键 sn */
    private Integer sn;
    /** 客户端 IP */
    private String ipAddr;
    private String country;
    private String province;
    private String city;
    private String area;
    private String detailPosition;
    private String isp;
    /** 根据 ip 接口查询失败次数，3 次之后不再请求 */
    private Integer tryTimes;
    /** 请求时间（yyyy-MM-dd HH:mm:ss.SSS） */
    private String reqTime;
    /** 响应时间（yyyy-MM-dd HH:mm:ss.SSS） */
    private String respTime;
    /** 耗时毫秒（字符串保留以兼容历史数据） */
    private String consumeTime;
    /** 请求 URL */
    private String reqUrl;
    /** 请求参数 */
    private String params;
    /** HTTP 方法 */
    private String reqMethod;
    /** 浏览器/UA 概要 */
    private String browser;
    /** 响应状态：0 成功；1 失败 */
    private String respStatus;
    /** 异常信息（失败时填充） */
    private String exceptMessage;

    public LogDO() {
    }

    public Integer getSn() {
        return sn;
    }

    public LogDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public LogDO setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public LogDO setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public LogDO setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCity() {
        return city;
    }

    public LogDO setCity(String city) {
        this.city = city;
        return this;
    }

    public String getArea() {
        return area;
    }

    public LogDO setArea(String area) {
        this.area = area;
        return this;
    }

    public String getDetailPosition() {
        return detailPosition;
    }

    public LogDO setDetailPosition(String detailPosition) {
        this.detailPosition = detailPosition;
        return this;
    }

    public String getIsp() {
        return isp;
    }

    public LogDO setIsp(String isp) {
        this.isp = isp;
        return this;
    }

    public Integer getTryTimes() {
        return tryTimes;
    }

    public LogDO setTryTimes(Integer tryTimes) {
        this.tryTimes = tryTimes;
        return this;
    }

    public String getReqTime() {
        return reqTime;
    }

    public LogDO setReqTime(String reqTime) {
        this.reqTime = reqTime;
        return this;
    }

    public String getRespTime() {
        return respTime;
    }

    public LogDO setRespTime(String respTime) {
        this.respTime = respTime;
        return this;
    }

    public String getConsumeTime() {
        return consumeTime;
    }

    public LogDO setConsumeTime(String consumeTime) {
        this.consumeTime = consumeTime;
        return this;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public LogDO setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
        return this;
    }

    public String getParams() {
        return params;
    }

    public LogDO setParams(String params) {
        this.params = params;
        return this;
    }

    public String getReqMethod() {
        return reqMethod;
    }

    public LogDO setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
        return this;
    }

    public String getBrowser() {
        return browser;
    }

    public LogDO setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    public String getRespStatus() {
        return respStatus;
    }

    public LogDO setRespStatus(String respStatus) {
        this.respStatus = respStatus;
        return this;
    }

    public String getExceptMessage() {
        return exceptMessage;
    }

    public LogDO setExceptMessage(String exceptMessage) {
        this.exceptMessage = exceptMessage;
        return this;
    }

    @Override
    public String toString() {
        return "LogDO{" +
                "sn=" + sn +
                ", ipAddr='" + ipAddr + '\'' +
                ", reqMethod='" + reqMethod + '\'' +
                ", reqUrl='" + reqUrl + '\'' +
                ", respStatus='" + respStatus + '\'' +
                ", reqTime='" + reqTime + '\'' +
                ", consumeTime='" + consumeTime + '\'' +
                '}';
    }
}
