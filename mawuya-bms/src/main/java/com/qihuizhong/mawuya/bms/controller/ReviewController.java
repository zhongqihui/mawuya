/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BMS 留言审批 controller。
 *
 * <p>菜单入口：左侧栏【留言管理 → 留言审批】（id=6）。</p>
 * <p>路由：</p>
 * <ul>
 *   <li>{@code GET  /bms/review/list.do?status=}    审批列表页（status: 空=待审核默认页, 0/1/2/all）</li>
 *   <li>{@code POST /bms/review/approve.do?sn=}    通过</li>
 *   <li>{@code POST /bms/review/reject.do?sn=}     拒绝</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/review")
public class ReviewController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    /**
     * 审批列表页：默认显示「待审核」（status=0），便于运营优先处理。
     * 查询参数 {@code status}：0/1/2 分别对应 PENDING/APPROVED/REJECTED；"all" 表示全部。
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

        // 不分页：单页最多 200 条，足够日常审批；后续若量大再加分页
        List<ReviewInfo> list = reviewService.listByStatus(statusFilter, 1, 200);

        model.addAttribute("reviewList", list);
        model.addAttribute("currentStatus", statusFilter == null ? "all" : String.valueOf(statusFilter));
        model.addAttribute("countPending",  reviewService.countByStatus(ReviewService.STATUS_PENDING));
        model.addAttribute("countApproved", reviewService.countByStatus(ReviewService.STATUS_APPROVED));
        model.addAttribute("countRejected", reviewService.countByStatus(ReviewService.STATUS_REJECTED));
        model.addAttribute("countAll",      reviewService.countByStatus(null));
        return "bms/review/review_list";
    }

    @PostMapping("approve.do")
    @ResponseBody
    public Map<String, Object> approve(@RequestParam("sn") Integer sn) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = reviewService.approve(sn);
        res.put("success", ok ? 1 : 0);
        res.put("message", ok ? "已通过" : "操作失败：评论不存在或更新失败");
        if (ok) {
            log.info("[bms/review] approve sn={}", sn);
        }
        return res;
    }

    @PostMapping("reject.do")
    @ResponseBody
    public Map<String, Object> reject(@RequestParam("sn") Integer sn) {
        Map<String, Object> res = new LinkedHashMap<>();
        boolean ok = reviewService.reject(sn);
        res.put("success", ok ? 1 : 0);
        res.put("message", ok ? "已拒绝" : "操作失败：评论不存在或更新失败");
        if (ok) {
            log.info("[bms/review] reject sn={}", sn);
        }
        return res;
    }
}
