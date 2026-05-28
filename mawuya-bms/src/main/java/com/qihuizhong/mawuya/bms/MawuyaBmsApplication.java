/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * bms（后台管理）启动类
 *
 * @author zqh
 */
@SpringBootApplication(scanBasePackages = {
        "com.qihuizhong.mawuya.core",
        "com.qihuizhong.mawuya.bms"
})
@MapperScan("com.qihuizhong.mawuya.core.mapper")
public class MawuyaBmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MawuyaBmsApplication.class, args);
    }
}
