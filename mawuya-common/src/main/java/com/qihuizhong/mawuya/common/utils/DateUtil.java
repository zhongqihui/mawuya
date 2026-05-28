/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author zqh
 */
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    /**
     * 截取时间字符串的年，如：2018-02-07 18:30:00 -> 2018
     */
    public static String getYear(String date) {
        return StringUtils.isNotEmpty(date) && date.length() >= 4 ? date.substring(0, 4) : "";
    }

    /**
     * 获取当前时间字符串
     */
    public static String getNowStr() {
        return new SimpleDateFormat(DEFAULT_PATTERN).format(new Date());
    }

    /**
     * 时间毫秒数 -> 字符串
     */
    public static String long2Str(Long l) {
        return new SimpleDateFormat(DEFAULT_PATTERN).format(new Date(l));
    }
}
