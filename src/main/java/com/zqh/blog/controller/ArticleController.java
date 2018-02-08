package com.zqh.blog.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.entity.Category;
import com.zqh.blog.service.ArticleService;
import com.zqh.blog.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/21 0021 下午 9:13
* description: 博客文章管理后台controller
**/
@Controller
@RequestMapping("bms/article")
public class ArticleController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    ArticleService articleService;
    @Autowired
    CategoryService categoryService;

    @PostMapping("login.do")
    public String login() {
        return "bms/index";
    }

    @GetMapping({"test","login"})
    public String index() {
        return "bms/index";
    }

    @GetMapping("toAdd.do")
    public String toWriteArticle(Model model) {
        List<Category> categories = categoryService.selectList(new HashMap());
        model.addAttribute("categoryList", categories);
        return "bms/article/writeArticle";
    }

    @PostMapping("addSubmit.do")
    @ResponseBody
    public String publishArticle(ArticleInfo articleInfo) {
        int count = articleService.insert(articleInfo);
        return "success";
    }


}
