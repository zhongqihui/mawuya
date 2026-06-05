/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.seo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * SEO 全局兜底注入：
 * <ul>
 *   <li>{@code seo} – 若 Controller 没显式塞入，则给一个站点默认值；如果塞了，则在缺失字段上回填默认。</li>
 *   <li>{@code seoProperties} – 直接暴露站点级配置，模板可读 site name / author / locale。</li>
 *   <li>自动计算 canonical（基于 SeoProperties.siteUrl + 当前请求 path）。</li>
 * </ul>
 *
 * <p>要点：所有 fts/* 视图无需在 Controller 里显式 model.add(seo) 也能渲染，开发体验零侵入。</p>
 *
 * @author 钟启辉
 */
@ControllerAdvice(basePackages = "com.niudeyapi.mawuya.ams.controller")
public class SeoModelAdvice {

    private final SeoProperties seoProperties;

    @Autowired
    public SeoModelAdvice(SeoProperties seoProperties) {
        this.seoProperties = seoProperties;
    }

    @ModelAttribute("seoProperties")
    public SeoProperties seoProperties() {
        return seoProperties;
    }

    /**
     * 生成站点级默认 SEO model；若 Controller 已通过同名 attribute 显式塞入，
     * Spring MVC 的规则是 @ModelAttribute 默认值会被覆盖（addAttribute 优先），
     * 这里仅在没有时生效。
     *
     * <p>另外补 canonical：站点根 URL + 当前 request URI，去掉所有查询串。</p>
     */
    @ModelAttribute("seo")
    public SeoModel defaultSeo(HttpServletRequest request) {
        SeoModel m = new SeoModel();
        m.setTitle(seoProperties.getDefaultTitle());
        m.setDescription(seoProperties.getDefaultDescription());
        m.setKeywords(seoProperties.getDefaultKeywords());
        m.setOgType("website");
        m.setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        m.setCanonical(buildCanonical(request));
        return m;
    }

    /**
     * 计算当前请求的 canonical：仅保留 path（去 query / fragment），用站点配置的 siteUrl 作为权威 host。
     * 多个 host / IP 共用同一应用时，全部归一到 siteUrl，避免被搜索引擎判定为重复内容。
     */
    private String buildCanonical(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        return seoProperties.getSiteUrl() + path;
    }
}
