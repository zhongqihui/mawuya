/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 标签 controller：标签云 + 单标签下文章列表。
 *
 * @author 钟启辉
 */
@Controller
public class TagController extends BaseController {

    private final TagService tagService;
    private final ArticleService articleService;

    @Autowired
    public TagController(TagService tagService, ArticleService articleService) {
        this.tagService = tagService;
        this.articleService = articleService;
    }

    @GetMapping("tags")
    public String tagCloud(Model model) {
        List<Tag> tags = tagService.getCloud();
        model.addAttribute("tags", tags);
        return "fts/tag_list";
    }

    @GetMapping("tags/{tid}")
    public String tagArticles(@PathVariable("tid") String tid, Model model) {
        Integer sn;
        try {
            sn = Integer.parseInt(tid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }
        Tag tag = tagService.selectById(sn);
        if (tag == null) {
            return ret404Page();
        }

        List<Integer> articleSns = tagService.getArticleSnByTag(sn);
        List<ArticleInfo> articles = articleService.listBySnList(articleSns);
        model.addAttribute("tag", tag)
                .addAttribute("articles", articles);
        return "fts/tag";
    }
}
