/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 后台主入口。
 *
 * <p>v4 起鉴权交由 Spring Security：</p>
 * <ul>
 *   <li>{@code GET /bms/login.do} 仅返回登录页（admin 校验由 Spring Security 接管）</li>
 *   <li>{@code POST /bms/login.do} 由 form-login 过滤器拦截，不再进入此 controller</li>
 *   <li>{@code GET /bms/index} 是登录成功后的目的地</li>
 * </ul>
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms")
public class MainController extends BaseController {

    /**
     * 登录页：把可能的 ?error=1 / ?logout=1 / ?expired=1 透传到模板，前端展示对应 toast。
     */
    @GetMapping("login.do")
    public String toLogin(@RequestParam(value = "error",   required = false) String error,
                          @RequestParam(value = "logout",  required = false) String logout,
                          @RequestParam(value = "expired", required = false) String expired,
                          Model model) {
        model.addAttribute("loginError",   error   != null);
        model.addAttribute("loginLogout",  logout  != null);
        model.addAttribute("loginExpired", expired != null);
        return "bms/adminLogin";
    }

    /** 登录成功后的主框架页 */
    @GetMapping("index")
    public String index() {
        return "bms/index";
    }

    @RequestMapping("main.do")
    public String toMain() {
        return "bms/common/main";
    }
}
