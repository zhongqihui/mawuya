/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller;

import com.niudeyapi.mawuya.core.service.SiteThemeService;
import com.niudeyapi.mawuya.bms.controller.api.ThemeApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AMS 主题切换视图 controller。
 *
 * <p>JSON 操作（apply）已迁移至
 * {@link ThemeApiController}。</p>
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
}
