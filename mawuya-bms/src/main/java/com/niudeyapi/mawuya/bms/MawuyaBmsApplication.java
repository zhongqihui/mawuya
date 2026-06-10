/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * bms（后台管理）启动类。
 *
 * <h2>时区初始化（重要）</h2>
 * <p>在 {@link SpringApplication#run} 之前显式 {@code TimeZone.setDefault(Asia/Shanghai)}，
 * 把整个 JVM 默认时区钉死为东八区。这一步是修复"日志记录时间 / 评论时间比真实时间慢
 * 8 小时"问题的<strong>根因兜底</strong>：</p>
 * <ul>
 *   <li>JDBC URL 已配 {@code serverTimezone=Asia/Shanghai}，MySQL 端 {@code NOW()} 没问题；</li>
 *   <li>但 Java 端 {@code SimpleDateFormat / LocalDateTime.now()} 没显式 timezone 时
 *       会走 JVM 默认时区。在 Docker UTC 容器里，这两套时间会差 8 小时；</li>
 *   <li>所以在主入口最早处统一 JVM 默认时区，从根上让全链路（DB + Java）时间一致；</li>
 *   <li>{@link com.niudeyapi.mawuya.common.utils.DateUtil} 内部还会再做一次防御性
 *       {@code setTimeZone}，保证即使有第三方代码修改了 default timezone，业务时间字段
 *       也不会被污染。</li>
 * </ul>
 *
 * @author zqh
 */
@SpringBootApplication(scanBasePackages = {
        "com.niudeyapi.mawuya.core",
        "com.niudeyapi.mawuya.bms"
})
@MapperScan("com.niudeyapi.mawuya.core.mapper")
@EnableScheduling
public class MawuyaBmsApplication {

    static {
        // 全链路统一 +08：必须在 Spring 容器启动前完成，否则容器内已实例化的
        // ScheduledExecutorService / 任何静态 SimpleDateFormat 会拿到旧时区。
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static void main(String[] args) {
        SpringApplication.run(MawuyaBmsApplication.class, args);
    }
}
