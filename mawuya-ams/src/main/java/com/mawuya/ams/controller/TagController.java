/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.controller;

import com.mawuya.ams.seo.SeoModel;
import com.mawuya.ams.seo.SeoProperties;
import com.mawuya.ams.seo.SeoUtils;
import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.dataobject.TagDO;
import com.mawuya.core.service.ArticleService;
import com.mawuya.core.service.TagService;
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
    private final SeoProperties seoProperties;

    @Autowired
    public TagController(TagService tagService,
                         ArticleService articleService,
                         SeoProperties seoProperties) {
        this.tagService = tagService;
        this.articleService = articleService;
        this.seoProperties = seoProperties;
    }

    @GetMapping("tags")
    public String tagCloud(Model model) {
        List<TagDO> tags = tagService.listTagCloud();
        model.addAttribute("tags", tags);

        SeoModel seo = SeoModel.of("标签云",
                "共有 " + (tags == null ? 0 : tags.size()) + " 个标签，按主题快速发现感兴趣的内容。")
                .setKeywords("标签,主题," + seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/tags")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("标签", null);
        model.addAttribute("seo", seo);
        return "fts/tag_list";
    }

    @GetMapping("tags/{tid:\\d+}")
    public String tagArticles(@PathVariable("tid") String tid, Model model) {
        Integer sn;
        try {
            sn = Integer.parseInt(tid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }
        TagDO tag = tagService.getById(sn);
        if (tag == null) {
            return ret404Page();
        }

        List<Integer> articleSns = tagService.listArticleSnByTag(sn);
        List<ArticleDO> articles = articleService.listBySnList(articleSns);
        model.addAttribute("tag", tag)
                .addAttribute("articles", articles);

        int n = articles == null ? 0 : articles.size();
        SeoModel seo = SeoModel.of("# " + tag.getTagName(),
                "标签「" + tag.getTagName() + "」共 " + n + " 篇文章。")
                .setKeywords(tag.getTagName() + "," + seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/tags/" + sn)
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("标签", seoProperties.getSiteUrl() + "/tags")
                .addBreadcrumb(tag.getTagName(), null);
        model.addAttribute("seo", seo);
        return "fts/tag";
    }
}
