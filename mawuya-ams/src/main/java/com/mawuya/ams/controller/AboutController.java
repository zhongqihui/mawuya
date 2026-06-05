/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.controller;

import com.mawuya.ams.seo.SeoModel;
import com.mawuya.ams.seo.SeoProperties;
import com.mawuya.ams.seo.SeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 关于页面。
 *
 * @author 钟启辉
 */
@Controller
public class AboutController extends BaseController {

    private final SeoProperties seoProperties;

    @Autowired
    public AboutController(SeoProperties seoProperties) {
        this.seoProperties = seoProperties;
    }

    @GetMapping("about")
    public String about(Model model) {
        SeoModel seo = SeoModel.of("关于",
                "关于 " + seoProperties.getSiteName() + " —— 作者背景、技术栈与联系方式。")
                .setKeywords("关于,作者,联系," + seoProperties.getDefaultKeywords())
                .setOgType("profile")
                .setCanonical(seoProperties.getSiteUrl() + "/about")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()))
                .addJsonLd(buildAboutJsonLd());
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("关于", null);
        model.addAttribute("seo", seo);
        return "fts/about";
    }

    /**
     * AboutPage + Person 结构化数据，便于搜索引擎识别站长身份。
     */
    private String buildAboutJsonLd() {
        return "{"
                + "\"@context\":\"https://schema.org\","
                + "\"@type\":\"AboutPage\","
                + "\"name\":\"关于 " + SeoUtils.escapeJson(seoProperties.getSiteName()) + "\","
                + "\"url\":\"" + seoProperties.getSiteUrl() + "/about\","
                + "\"mainEntity\":{"
                + "\"@type\":\"Person\","
                + "\"name\":\"" + SeoUtils.escapeJson(seoProperties.getAuthor()) + "\","
                + "\"url\":\"" + seoProperties.getSiteUrl() + "/about\""
                + "}}";
    }
}
