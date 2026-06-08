/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * BMS 全局视图属性注入。
 *
 * <p>把跨页面共享的站点级配置统一暴露给所有 Thymeleaf 视图，杜绝模板里硬编码：</p>
 * <ul>
 *   <li>{@code ${amsSiteUrl}} —— AMS 前台根 URL（来源：{@code mawuya.ams.site-url}）；
 *       模板里拼接前台跳转链接</li>
 *   <li>{@code ${siteName}}  —— 站点显示名（来源：{@code mawuya.seo.site-name}，
 *       与 AMS {@link com.niudeyapi.mawuya.ams.seo.SeoProperties#getSiteName} 同一 key，
 *       一处配置两端生效，杜绝双写）；模板里渲染 brand / title</li>
 * </ul>
 *
 * <p>设计：</p>
 * <ul>
 *   <li>仅对 BMS controller 包生效，避免污染未来可能引入的其它 web 模块</li>
 *   <li>本机开发：使用各项的默认值，无需任何配置</li>
 *   <li>上线：通过环境变量（{@code MAWUYA_AMS_SITE_URL}、{@code MAWUYA_SEO_SITE_NAME}）覆盖即可</li>
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
     * 站点显示名：与 AMS SeoProperties.siteName 同一配置 key（{@code mawuya.seo.site-name}），
     * 整个项目只在 yml 维护一处。BMS 模板内的 brand、登录页 title、css-head title 都从这里读。
     */
    @Value("${mawuya.seo.site-name:My Blog}")
    private String siteName;

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

    /** 暴露给所有 BMS 视图的站点显示名；空值兜底为 "My Blog" 避免模板出现空字符 */
    @ModelAttribute("siteName")
    public String siteName() {
        return (siteName == null || siteName.isEmpty()) ? "My Blog" : siteName;
    }
}
