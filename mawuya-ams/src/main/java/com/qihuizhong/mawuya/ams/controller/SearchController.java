/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 全文搜索 controller。
 *
 * @author 钟启辉
 */
@Controller
public class SearchController extends BaseController {

    private final ArticleService articleService;

    @Autowired
    public SearchController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @RequestMapping("search")
    public String search(String q, Model model, HttpServletRequest request) {
        if (q == null) {
            q = "";
        }
        // 限制最长 60，避免过长查询拖垮 DB
        String keyword = q.length() > 60 ? q.substring(0, 60) : q;

        Page<ArticleInfo> page;
        if (keyword.trim().isEmpty()) {
            page = new Page<>();
            page.setLists(java.util.Collections.emptyList())
                    .setCurr(1).setSize(10).setPageSize(0);
        } else {
            page = articleService.search(keyword.trim(), request);
        }
        page.setUrl("search");
        model.addAttribute("page", page)
                .addAttribute("keyword", keyword);
        return "fts/search";
    }
}
