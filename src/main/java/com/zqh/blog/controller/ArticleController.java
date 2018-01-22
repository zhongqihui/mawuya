package com.zqh.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/21 0021 下午 9:13
* description: 博客文章管理后台controller
**/
@Controller
@RequestMapping("bms")
public class ArticleController {

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

}
