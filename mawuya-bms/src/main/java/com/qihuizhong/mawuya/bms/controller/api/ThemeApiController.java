/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller.api;

import com.qihuizhong.mawuya.bms.dto.request.ThemeApplyRequest;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.service.SiteThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 主题切换 JSON API（{@code POST /bms/api/theme/apply}）。
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/theme")
public class ThemeApiController {

    private final SiteThemeService siteThemeService;

    @Autowired
    public ThemeApiController(SiteThemeService siteThemeService) {
        this.siteThemeService = siteThemeService;
    }

    @PostMapping("apply")
    public BaseResponse<Void> apply(@Valid ThemeApplyRequest req) {
        if (!siteThemeService.applyTheme(req.getCode())) {
            throw new BusinessException("主题切换失败：code 不在允许范围");
        }
        return BaseResponse.success("已切换主题", null);
    }
}
