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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 评论提交 controller。
 *
 * <p>表单提交后跳回原文章详情页（带 #comments 锚点）。
 * 评论默认进入「待审核」状态，由 BMS 审批通过后才会在前台展示；
 * 故提交后通过 flash attribute 把提示信息（成功 or 校验错误）带回详情页。</p>
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
                         RedirectAttributes ra) {
        if (articleSn == null) {
            return ret404Page();
        }
        String err = reviewService.submit(articleSn, name, content);
        if (err == null || err.isEmpty()) {
            ra.addFlashAttribute("commentTip", "评论已提交，待管理员审核通过后将公开展示。");
            ra.addFlashAttribute("commentTipType", "success");
        } else {
            ra.addFlashAttribute("commentTip", err);
            ra.addFlashAttribute("commentTipType", "error");
        }
        return "redirect:/" + articleSn + "#comments";
    }
}
