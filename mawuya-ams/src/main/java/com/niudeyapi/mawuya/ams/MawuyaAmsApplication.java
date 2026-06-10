/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * ams（前台）启动类。
 *
 * <h2>时区初始化（重要）</h2>
 * <p>在 {@link SpringApplication#run} 之前显式 {@code TimeZone.setDefault(Asia/Shanghai)}，
 * 与 BMS 启动类保持一致。这是修复"日志/评论时间慢 8 小时"问题的根因兜底：</p>
 * <ul>
 *   <li>访客日志 {@code req_time / resp_time} 在 {@link com.niudeyapi.mawuya.core.interceptor.LogInterceptor}
 *       里通过 {@code DateUtil.long2Str()} 格式化；如果 JVM 默认时区是 UTC，
 *       格式化结果会比墙上时间慢 8 小时；</li>
 *   <li>评论时间走 MySQL {@code NOW()}，JDBC 已钉死 +08，不受 JVM 影响；</li>
 *   <li>两套时间不一致就会出现 BMS 日志页 "刚刚的访问被显示为 8 小时前" 的故障。</li>
 * </ul>
 * <p>在主入口最早处把 JVM 默认时区也钉死为 +08，确保 Java 端格式化与 DB 端 NOW()
 * 取值完全对齐。{@link com.niudeyapi.mawuya.common.utils.DateUtil} 内部再做一次
 * 防御性 {@code setTimeZone}，双保险。</p>
 *
 * @author zqh
 */
@SpringBootApplication(scanBasePackages = {
        "com.niudeyapi.mawuya.core",
        "com.niudeyapi.mawuya.ams"
})
@MapperScan("com.niudeyapi.mawuya.core.mapper")
public class MawuyaAmsApplication {

    static {
        // 全链路统一 +08：必须在 Spring 容器启动前完成
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static void main(String[] args) {
        SpringApplication.run(MawuyaAmsApplication.class, args);
    }
}
