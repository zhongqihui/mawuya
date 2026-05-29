/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台文章管理视图 controller。
 *
 * <p>文章 CRUD / 上传等 JSON 操作已迁移至
 * {@link com.qihuizhong.mawuya.bms.controller.api.ArticleApiController}。</p>
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms/article")
public class ArticleController extends BaseController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryService categoryService;

    /** 写文章页 */
    @GetMapping("toAdd.do")
    public String toWriteArticle(Model model) {
        List<Category> categories = categoryService.selectList(new HashMap<>());
        model.addAttribute("categoryList", categories);
        return "bms/article/write_article";
    }

    /** 文章列表页（数据由 model 直出，本身不分页） */
    @GetMapping("list.do")
    public String articleList(Model model) {
        Map<String, String> map = new HashMap<>();
        List<ArticleInfo> articleList = articleService.getAllNoContent(map);
        List<Category> categoryList = categoryService.selectList(map);

        model.addAttribute("articleList", articleList)
                .addAttribute("categoryList", categoryList);
        return "bms/article/article_list";
    }

    /** 编辑文章页 */
    @GetMapping("toUpdate.do")
    public String toUpdate(@RequestParam("sn") String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        ArticleInfo articleInfo = articleService.selectById(id);
        if (articleInfo == null) {
            throw new BusinessException("文章不存在");
        }
        List<Category> categories = categoryService.selectList(new HashMap<>());
        model.addAttribute("categoryList", categories)
                .addAttribute("article", articleInfo);
        return "bms/article/mod_article";
    }
}
