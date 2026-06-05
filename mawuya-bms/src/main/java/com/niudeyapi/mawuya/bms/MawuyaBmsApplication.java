/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * bms（后台管理）启动类
 *
 * @author zqh
 */
@SpringBootApplication(scanBasePackages = {
        "com.niudeyapi.mawuya.core",
        "com.niudeyapi.mawuya.bms"
})
@MapperScan("com.niudeyapi.mawuya.core.mapper")
public class MawuyaBmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MawuyaBmsApplication.class, args);
    }
}
