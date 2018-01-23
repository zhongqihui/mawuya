package com.zqh.blog.controller;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/21 0021 下午 9:13
* description: 博客文章管理后台controller
**/
@Controller
@RequestMapping("bms")
public class ArticleController {
    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    ArticleService articleService;

    @PostMapping("login.do")
    public String login() {
        return "bms/index";
    }

    @GetMapping({"test","login"})
    public String index() {
        return "bms/index";
    }

    @GetMapping("writeArticle.do")
    public String toWriteArticle() {
        return "bms/article/writeArticle";
    }

    @PostMapping("writeArticle.do")
    @ResponseBody
    public String publishArticle(ArticleInfo articleInfo) {
        int count = articleService.insert(articleInfo);
        return "success";
    }

}
