/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.controller.api;

import com.mawuya.bms.dto.request.SnRequest;
import com.mawuya.core.common.BaseResponse;
import com.mawuya.core.enums.ResultCodeEnum;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 留言审批 JSON API。
 *
 * <h3>API 列表</h3>
 * <ul>
 *   <li>{@code POST /bms/api/review/approve}  通过评论</li>
 *   <li>{@code POST /bms/api/review/reject}   拒绝评论</li>
 * </ul>
 *
 * <p>统一返回 {@link BaseResponse}：成功 {@code data=null}，失败抛 {@link BusinessException}
 * 由 {@code GlobalExceptionHandler} 兜底转换。</p>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/review")
public class ReviewApiController {

    private static final Logger log = LoggerFactory.getLogger(ReviewApiController.class);

    @Autowired
    private ReviewService reviewService;

    @PostMapping("approve")
    public BaseResponse<Void> approve(@Valid SnRequest req) {
        if (!reviewService.approve(req.getSn())) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "评论不存在或更新失败");
        }
        log.info("[bms/review] approve sn={}", req.getSn());
        return BaseResponse.success("已通过", null);
    }

    @PostMapping("reject")
    public BaseResponse<Void> reject(@Valid SnRequest req) {
        if (!reviewService.reject(req.getSn())) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "评论不存在或更新失败");
        }
        log.info("[bms/review] reject sn={}", req.getSn());
        return BaseResponse.success("已拒绝", null);
    }
}
