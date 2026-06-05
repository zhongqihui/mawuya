/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.controller;

import com.niudeyapi.mawuya.ams.seo.SeoModel;
import com.niudeyapi.mawuya.ams.seo.SeoProperties;
import com.niudeyapi.mawuya.ams.seo.SeoUtils;
import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.service.ArticleService;
import com.niudeyapi.mawuya.core.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 全文搜索 controller。
 *
 * <p>搜索结果页统一打 noindex —— 这是 SEO 最佳实践：搜索结果页对索引价值低、易被
 * 判定为"thin content"，统一拒收避免参数化大量重复页污染索引。</p>
 *
 * @author 钟启辉
 */
@Controller
public class SearchController extends BaseController {

    private final ArticleService articleService;
    private final SeoProperties seoProperties;

    @Autowired
    public SearchController(ArticleService articleService, SeoProperties seoProperties) {
        this.articleService = articleService;
        this.seoProperties = seoProperties;
    }

    @RequestMapping("search")
    public String search(String q, Model model, HttpServletRequest request) {
        if (q == null) {
            q = "";
        }
        // 限制最长 60，避免过长查询拖垮 DB
        String keyword = q.length() > 60 ? q.substring(0, 60) : q;

        Page<ArticleDO> page;
        if (keyword.trim().isEmpty()) {
            page = new Page<>();
            page.setLists(java.util.Collections.emptyList())
                    .setCurr(1).setSize(10).setPageSize(0);
        } else {
            page = articleService.searchByKeyword(keyword.trim(), request);
        }
        page.setUrl("search");
        model.addAttribute("page", page)
                .addAttribute("keyword", keyword);

        String title = keyword.isEmpty() ? "搜索" : "搜索：" + keyword;
        SeoModel seo = SeoModel.of(title, "站内搜索结果页面。")
                .setKeywords(seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/search")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()))
                .setNoindex(true);
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("搜索", null);
        model.addAttribute("seo", seo);
        return "fts/search";
    }
}
