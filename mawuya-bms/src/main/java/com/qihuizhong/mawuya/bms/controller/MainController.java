/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后台登录入口
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms")
public class MainController extends BaseController {

    @GetMapping("login.do")
    public String toLogin() {
        return "bms/adminLogin";
    }

    /**
     * TODO: 当前为硬编码鉴权，仅满足原工程行为；后续应替换为 Spring Security 或自定义鉴权方案。
     */
    @PostMapping("login.do")
    public String loginSubmit(String userName, String password) {
        if ("admin".equals(userName) && "123456".equals(password)) {
            return "bms/index";
        }
        return "bms/adminLogin";
    }

    @RequestMapping("main.do")
    public String toMain() {
        return "bms/common/main";
    }
}
