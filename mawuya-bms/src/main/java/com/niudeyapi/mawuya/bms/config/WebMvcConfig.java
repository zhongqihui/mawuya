/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * bms Web 配置：仅静态资源 + 上传目录映射。
 *
 * @author zqh
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${mawuya.upload.dir}")
    private String uploadDir;

    @Value("${mawuya.upload.url-prefix}")
    private String uploadUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/statics/**")
                .addResourceLocations("classpath:/static/statics/");

        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations("file:" + location);
    }
}
