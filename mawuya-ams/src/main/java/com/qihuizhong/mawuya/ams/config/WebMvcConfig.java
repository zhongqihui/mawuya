/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.config;

import com.qihuizhong.mawuya.core.interceptor.LogInterceptor;
import com.qihuizhong.mawuya.core.listener.MySessionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 前台 Web 配置：注册访客拦截器、Session 监听器，挂载上传目录静态映射。
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
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/bms/**",
                        "/statics/**",
                        "/upload/**",
                        "/favicon.ico",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源（与原工程目录保持一致）
        registry.addResourceHandler("/statics/**")
                .addResourceLocations("classpath:/static/statics/");

        // 上传文件目录
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations("file:" + location);
    }

    @Bean
    public ServletListenerRegistrationBean<MySessionListener> sessionListenerRegistration() {
        ServletListenerRegistrationBean<MySessionListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new MySessionListener());
        return bean;
    }
}
