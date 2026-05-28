/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 关于页面。
 *
 * @author 钟启辉
 */
@Controller
public class AboutController extends BaseController {

    @GetMapping("about")
    public String about() {
        return "fts/about";
    }
}
