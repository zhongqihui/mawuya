/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.controller;

import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.dataobject.CategoryDO;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.service.ArticleService;
import com.mawuya.core.service.CategoryService;
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
 * {@link com.mawuya.bms.controller.api.ArticleApiController}。</p>
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
        List<CategoryDO> categories = categoryService.list(new HashMap<>(4));
        model.addAttribute("categoryList", categories);
        return "bms/article/write_article";
    }

    /** 文章列表页（数据由 model 直出，本身不分页） */
    @GetMapping("list.do")
    public String articleList(Model model) {
        Map<String, String> map = new HashMap<>(4);
        List<ArticleDO> articleList = articleService.listAllNoContent(map);
        List<CategoryDO> categoryList = categoryService.list(map);

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

        ArticleDO articleInfo = articleService.getById(id);
        if (articleInfo == null) {
            throw new BusinessException("文章不存在");
        }
        List<CategoryDO> categories = categoryService.list(new HashMap<>(4));
        model.addAttribute("categoryList", categories)
                .addAttribute("article", articleInfo);
        return "bms/article/mod_article";
    }
}
