/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller;

import com.niudeyapi.mawuya.bms.controller.api.LogApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * BMS 访客日志视图 controller（仅渲染骨架，数据由 API 异步拉取）。
 *
 * <p>JSON 接口已迁移至
 * {@link LogApiController}。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/log")
public class LogController extends BaseController {

    @GetMapping("list.do")
    public String list() {
        return "bms/log/log_list";
    }
}
