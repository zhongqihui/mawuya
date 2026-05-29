/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * BMS 留言审批视图 controller。
 *
 * <p>JSON 操作（approve/reject/列表数据）已迁移至 {@link com.qihuizhong.mawuya.bms.controller.api.ReviewApiController}，
 * 本类仅负责渲染列表页骨架（Thymeleaf 视图）。</p>
 *
 * <p>菜单入口：左侧栏【留言管理 → 留言审批】（id=6）。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/review")
public class ReviewController extends BaseController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 审批列表页：默认显示「待审核」（status=0），便于运营优先处理。
     * 查询参数 {@code status}：0/1/2 分别对应 PENDING/APPROVED/REJECTED；"all" 表示全部。
     *
     * <p>页面初次渲染会带上当前状态对应的列表数据；前端 ajax 操作（通过/拒绝）成功后
     * 会通过 {@code /bms/api/review/page} 重新刷新数据。</p>
     */
    @GetMapping("list.do")
    public String list(@RequestParam(value = "status", required = false, defaultValue = "0") String status,
                       Model model) {
        Integer statusFilter;
        if ("all".equalsIgnoreCase(status)) {
            statusFilter = null;
        } else {
            try {
                int s = Integer.parseInt(status);
                statusFilter = (s >= 0 && s <= 2) ? s : Integer.valueOf(0);
            } catch (NumberFormatException e) {
                statusFilter = 0;
            }
        }

        List<ReviewInfo> list = reviewService.listByStatus(statusFilter, 1, 200);

        model.addAttribute("reviewList", list);
        model.addAttribute("currentStatus", statusFilter == null ? "all" : String.valueOf(statusFilter));
        model.addAttribute("countPending",  reviewService.countByStatus(ReviewService.STATUS_PENDING));
        model.addAttribute("countApproved", reviewService.countByStatus(ReviewService.STATUS_APPROVED));
        model.addAttribute("countRejected", reviewService.countByStatus(ReviewService.STATUS_REJECTED));
        model.addAttribute("countAll",      reviewService.countByStatus(null));
        return "bms/review/review_list";
    }
}
