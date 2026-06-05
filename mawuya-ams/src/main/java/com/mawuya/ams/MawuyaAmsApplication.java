/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ams（前台）启动类
 *
 * @author zqh
 */
@SpringBootApplication(scanBasePackages = {
        "com.mawuya.core",
        "com.mawuya.ams"
})
@MapperScan("com.mawuya.core.mapper")
public class MawuyaAmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MawuyaAmsApplication.class, args);
    }
}
