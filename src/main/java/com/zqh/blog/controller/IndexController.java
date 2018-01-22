package com.zqh.blog.controller;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/21 0021 下午 9:13
 * description: 博客前台controller，用于展示
 **/
@Controller
public class IndexController {
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    ArticleService articleService;

    @RequestMapping({"/", "index.html", "index", "index.jsp"})
    public String toHomePage() {

        List<ArticleInfo> articles = articleService.getArticles();
        log.info("" + articles.size());
        System.out.println(articles.size());
        return "forward:index.jsp";
    }
}
