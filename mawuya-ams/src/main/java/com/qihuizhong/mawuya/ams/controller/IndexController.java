/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.listener.MySessionContext;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.service.CategoryService;
import com.qihuizhong.mawuya.core.service.ReviewService;
import com.qihuizhong.mawuya.core.service.TagService;
import com.qihuizhong.mawuya.core.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台 controller：首页 / 详情 / 归档 / 分类。
 *
 * @author 钟启辉
 */
@Controller
public class IndexController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ReviewService reviewService;

    @Autowired
    public IndexController(ArticleService articleService,
                           CategoryService categoryService,
                           TagService tagService,
                           ReviewService reviewService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.reviewService = reviewService;
    }

    /**
     * 博客首页（GET 与 POST 都支持，POST 是分页表单提交）
     */
    @RequestMapping(value = {"/", "index.html", "index", "index.jsp"})
    public String toHomePage(Model model, HttpServletRequest request) {
        Page<ArticleInfo> page = articleService.getPage(request);
        page.setUrl("index");

        List<Category> categoryList = categoryService.selectList(new HashMap<>());
        model.addAttribute("page", page)
                .addAttribute("categoryList", categoryList);
        return "fts/index";
    }

    @RequestMapping("favicon")
    public String favicon() {
        return null;
    }

    /**
     * 文章详情：含上一篇 / 下一篇 / 分类 / 标签 / 评论列表
     */
    @RequestMapping("{aid}")
    public String getArticle(@PathVariable("aid") String aid, Model model, HttpServletRequest request) {
        Integer sn;
        try {
            sn = Integer.parseInt(aid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        MySessionContext.getInstance().addArticleSn2Session(request.getSession(), aid);

        ArticleInfo info = articleService.selectById(sn);
        if (info == null) {
            return ret404Page();
        }

        ArticleInfo next = articleService.getNext(sn);
        ArticleInfo prev = articleService.getPrev(sn);
        Category c = categoryService.selectById(info.getCategorySn());
        List<Tag> tags = tagService.getByArticleSn(sn);
        List<ReviewInfo> reviews = reviewService.listByArticle(sn);

        model.addAttribute("article", info)
                .addAttribute("next", next)
                .addAttribute("prev", prev)
                .addAttribute("category", c)
                .addAttribute("tags", tags)
                .addAttribute("reviews", reviews);
        return "fts/show_article";
    }

    /**
     * 归档页面
     */
    @GetMapping("archive")
    public String toArchive(Model model) {
        Map<String, List<ArticleInfo>> map = articleService.getYearMap();
        int count = articleService.getCount(new HashMap<>());
        model.addAttribute("map", map).addAttribute("count", count);
        return "fts/archive";
    }

    /**
     * 博客分类列表
     */
    @GetMapping("categories")
    public String categoryList(Model model) {
        List<Category> categoryList = categoryService.getCategoryList();
        model.addAttribute("categoryList", categoryList);
        return "fts/category_list";
    }

    /**
     * 博客分类下的文章列表
     */
    @RequestMapping("categories/{cid}")
    public String getCategory(@PathVariable("cid") String cid, Model model) {
        Integer sn;
        try {
            sn = Integer.parseInt(cid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        Category category = categoryService.getCategoryBySn(sn);
        if (category == null) {
            return ret404Page();
        }
        model.addAttribute("category", category);
        return "fts/category";
    }
}
