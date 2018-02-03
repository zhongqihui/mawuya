package com.zqh.blog.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/1 10:44
 * description:
 **/
@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class LogInfo implements Serializable {
    private Integer sn;

    /*** ip地址*/
    private String ipAddr;

    /*** 国家*/
    private String state;

    /*** 省份*/
    private String province;

    /*** 城市*/
    private String city;

    /*** 区 */
    private String area;

    /*** 详细地理位置 */
    private String detailPosition;

    /*** 请求时间*/
    private String reqTime;

    /*** 响应时间*/
    private String respTime;

    /*** 请求耗时*/
    private Long consumeTime;

    /*** 用户请求的url路径*/
    private String reqUrl;

    /*** 用户请求请求的参数*/
    private String params;

    /*** 用户请求的方式，Get or Post or ...*/
    private String reqMethod;

    /*** 用户请求的浏览器信息*/
    private String browser;

    /*** 响应状态，0表示成功；1表示失败，如抛异常响应500等*/
    private String respStatus;

    /*** 异常信息，success表示成功*/
    private String exceptMessage;

}
