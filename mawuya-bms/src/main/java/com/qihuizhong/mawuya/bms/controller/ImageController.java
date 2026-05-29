/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后台图片管理视图 controller（仅渲染骨架）。
 *
 * <p>JSON 操作（上传 / 列表 / 删除）已迁移至
 * {@link com.qihuizhong.mawuya.bms.controller.api.ImageApiController}。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/image")
public class ImageController extends BaseController {

    @GetMapping("library.do")
    public String library() {
        return "bms/image/library";
    }

    /**
     * 图片选择器弹窗页（独立页面，被 layer iframe 加载）。
     * 与图片库主页解耦：仅做"挑选"，不含上传/删除等管理操作。
     */
    @GetMapping("picker.do")
    public String picker() {
        return "bms/image/picker";
    }
}
