/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import com.qihuizhong.mawuya.core.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 评论提交 controller。
 *
 * <p>表单提交后跳回原文章详情页（带 #comments 锚点）。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("comments")
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("submit")
    public String submit(@RequestParam("articleSn") Integer articleSn,
                         @RequestParam("name") String name,
                         @RequestParam("content") String content,
                         HttpServletRequest request) {
        if (articleSn == null) {
            return ret404Page();
        }
        // 简单防灌水：内容/昵称由 Service 层校验
        reviewService.submit(articleSn, name, content);
        // 不论成功失败都回到详情页，错误提示通过 service 返回信息可在更高级版本中通过 flash attr 展示
        return "redirect:/" + articleSn + "#comments";
    }
}
