/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

/**
 * bms controller 基类
 *
 * @author zqh
 */
public abstract class BaseController {

    public String ret404Page() {
        return "pub/404";
    }
}
