/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期工具类。
 *
 * <h2>时区策略（重要）</h2>
 * <p>项目所有"业务时间字符串"统一按 <b>东八区 / Asia/Shanghai</b> 输出，
 * 与 MySQL JDBC URL 中的 {@code serverTimezone=Asia/Shanghai} 对齐。
 * 这里显式 {@code setTimeZone} 是防御性写法：即便部署机 JVM 默认时区不是 +08
 * （典型如 Docker 容器默认 UTC），日志与评论的展示时间依然是用户预期的北京时间，
 * 不会再出现"日志时间比真实时间慢 8 小时"的故障。</p>
 *
 * <p>历史问题（已修复）：本工具原使用 {@code new SimpleDateFormat(...)} 不带时区，
 * 默认走 {@code TimeZone.getDefault()}；在 UTC 环境下 {@link #getNowStr()} /
 * {@link #long2Str(Long)} 输出比墙上时间慢 8h，而评论 {@code NOW()} 走 MySQL
 * session（被 JDBC 钉死 +08），两套时间不一致，导致 BMS 日志页"刚刚"的访问
 * 显示为 8 小时前。修复方式：所有 SimpleDateFormat 显式 setTimeZone +08。</p>
 *
 * @author zqh
 */
public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 业务统一时区：Asia/Shanghai（UTC+8）。
     * <p>与各 application.yml 中 {@code serverTimezone=Asia/Shanghai} 对齐，
     * 保证 Java 端格式化时间与 DB 端 {@code NOW()} 取值落在同一时区。</p>
     */
    public static final TimeZone DEFAULT_TZ = TimeZone.getTimeZone("Asia/Shanghai");

    private DateUtil() {
    }

    /**
     * 截取时间字符串的年，如：2018-02-07 18:30:00 -> 2018
     */
    public static String getYear(String date) {
        return StringUtils.isNotEmpty(date) && date.length() >= 4 ? date.substring(0, 4) : "";
    }

    /**
     * 获取当前时间字符串（东八区，格式 {@value #DEFAULT_PATTERN}）
     */
    public static String getNowStr() {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN);
        sdf.setTimeZone(DEFAULT_TZ);
        return sdf.format(new Date());
    }

    /**
     * 时间毫秒数 -> 字符串（东八区，格式 {@value #DEFAULT_PATTERN}）
     */
    public static String long2Str(Long l) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_PATTERN);
        sdf.setTimeZone(DEFAULT_TZ);
        return sdf.format(new Date(l));
    }
}
