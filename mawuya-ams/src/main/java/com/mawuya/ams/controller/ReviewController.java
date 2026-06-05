/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.controller;

import com.mawuya.ams.dto.request.ReviewSubmitRequest;
import com.mawuya.core.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 评论提交 controller（视图重定向，非 JSON）。
 *
 * <p>表单提交后跳回原文章详情页（带 #comments 锚点）。
 * 评论默认进入「待审核」状态，由 BMS 审批通过后才会在前台展示；
 * 故提交后通过 flash attribute 把提示信息（成功 or 校验错误）带回详情页。</p>
 *
 * <h3>API 规范化说明</h3>
 * <p>入参由 {@link ReviewSubmitRequest} 承接 form-urlencoded 字段；本接口属于"表单跳转"
 * 类型，<strong>不</strong>使用 {@code BaseResponse} —— 因为它返回的是 view name（重定向），
 * 不是 JSON。校验失败由 service 层产生中文提示并通过 flash 写回 UI。</p>
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
    public String submit(ReviewSubmitRequest req, RedirectAttributes ra) {
        if (req.getArticleSn() == null) {
            return ret404Page();
        }
        String err = reviewService.submit(req.getArticleSn(), req.getName(), req.getContent());
        if (err == null || err.isEmpty()) {
            ra.addFlashAttribute("commentTip", "评论已提交，待管理员审核通过后将公开展示。");
            ra.addFlashAttribute("commentTipType", "success");
        } else {
            ra.addFlashAttribute("commentTip", err);
            ra.addFlashAttribute("commentTipType", "error");
        }
        return "redirect:/" + req.getArticleSn() + "#comments";
    }
}
