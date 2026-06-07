/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * BMS 全局视图属性注入：
 * <p>
 * 把 {@code mawuya.ams.site-url}（AMS 前台站点根地址，无尾斜杠）注入到所有 Thymeleaf 视图，
 * 模板里通过 {@code ${amsSiteUrl}} 拼接前台跳转链接，杜绝硬编码 {@code http://localhost:8080}。
 * <ul>
 *   <li>仅对 BMS controller 包生效，避免污染未来可能引入的其它 web 模块</li>
 *   <li>本机开发：默认 http://localhost:8080，无需任何配置</li>
 *   <li>上线：通过环境变量 {@code MAWUYA_AMS_SITE_URL=https://your-domain} 覆盖即可</li>
 * </ul>
 *
 * @author 钟启辉
 */
@ControllerAdvice(basePackages = "com.niudeyapi.mawuya.bms.controller")
public class BmsGlobalModelAttributes {

    /** 由 application.yml 的 mawuya.ams.site-url 注入；上线务必通过 env 覆盖默认值 */
    @Value("${mawuya.ams.site-url:http://localhost:8080}")
    private String amsSiteUrl;

    /**
     * 暴露给所有 BMS 视图的 AMS 前台 base URL。
     * 已 normalize：去掉末尾斜杠，模板使用形如 {@code |${amsSiteUrl}/${sn}|} 即可。
     */
    @ModelAttribute("amsSiteUrl")
    public String amsSiteUrl() {
        if (amsSiteUrl == null || amsSiteUrl.isEmpty()) {
            return "";
        }
        // 去尾斜杠，避免拼接时出现双斜杠
        return amsSiteUrl.endsWith("/")
                ? amsSiteUrl.substring(0, amsSiteUrl.length() - 1)
                : amsSiteUrl;
    }
}
