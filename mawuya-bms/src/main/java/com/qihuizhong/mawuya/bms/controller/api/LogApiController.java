/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller.api;

import com.qihuizhong.mawuya.bms.dto.request.LogQueryRequest;
import com.qihuizhong.mawuya.bms.dto.request.SnRequest;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import com.qihuizhong.mawuya.core.common.PageResponse;
import com.qihuizhong.mawuya.core.entity.LogInfo;
import com.qihuizhong.mawuya.core.enums.ResultCodeEnum;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.service.LogInfoService;
import com.qihuizhong.mawuya.core.vo.LogInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 访客日志查询 JSON API。
 *
 * <ul>
 *   <li>{@code GET /bms/api/log/page}    多条件 + 分页查询</li>
 *   <li>{@code GET /bms/api/log/detail}  单条详情</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/log")
public class LogApiController {

    @Autowired
    private LogInfoService logInfoService;

    @GetMapping("page")
    public BaseResponse<PageResponse<LogInfo>> page(@Valid LogQueryRequest req) {
        LogInfoQuery q = new LogInfoQuery()
                .setIpAddr(req.getIpAddr())
                .setReqUrl(req.getReqUrl())
                .setReqMethod(req.getReqMethod())
                .setRespStatus(req.getRespStatus())
                .setCountry(req.getCountry())
                .setProvince(req.getProvince())
                .setStartTime(req.getStartTime())
                .setEndTime(req.getEndTime())
                .setOrderField(req.getOrderField())
                .setOrderDir(req.getOrderDir());

        int total = logInfoService.count(q);
        List<LogInfo> list = logInfoService.queryPage(q, req.getPage(), req.getSize());
        return BaseResponse.success(PageResponse.of(list, total, req.getPage(), req.getSize()));
    }

    @GetMapping("detail")
    public BaseResponse<LogInfo> detail(@Valid SnRequest req) {
        LogInfo info = logInfoService.getById(req.getSn());
        if (info == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "记录不存在");
        }
        return BaseResponse.success(info);
    }
}
