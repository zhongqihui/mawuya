/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.service.SiteThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * AMS 主题切换管理（一键换皮）。
 *
 * <p>读：list.do 渲染主题卡片页（4 套 + 当前 code）；
 *    写：apply.do 由前端 ajax 触发，落库 blog_info.theme_code 即生效（AMS 下次请求注入新值）。</p>
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms/theme")
public class ThemeController extends BaseController {

    private final SiteThemeService siteThemeService;

    @Autowired
    public ThemeController(SiteThemeService siteThemeService) {
        this.siteThemeService = siteThemeService;
    }

    @GetMapping("list.do")
    public String list(Model model) {
        model.addAttribute("themes", siteThemeService.listThemes());
        model.addAttribute("currentCode", siteThemeService.getCurrentTheme());
        return "bms/theme/theme_list";
    }

    @PostMapping("apply.do")
    @ResponseBody
    public String apply(@RequestParam("code") String code) {
        return siteThemeService.applyTheme(code) ? "success" : "fail";
    }
}
