/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 博客信息实体（与 blog_info 单例表对应，固定 id = 1）
 *
 * <p>承载站点级配置，例如：博主名、AMS 当前皮肤主题 code 等。</p>
 *
 * @author zqh
 */
public class BlogInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单例主键，固定为 1 */
    private Integer id;

    /** 博主名 */
    private String bloggerName;

    /** AMS 当前主题 code：default / tech-dark / gradient-vivid / minimal-business */
    private String themeCode;

    public BlogInfo() {
    }

    public Integer getId() {
        return id;
    }

    public BlogInfo setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getBloggerName() {
        return bloggerName;
    }

    public BlogInfo setBloggerName(String bloggerName) {
        this.bloggerName = bloggerName;
        return this;
    }

    public String getThemeCode() {
        return themeCode;
    }

    public BlogInfo setThemeCode(String themeCode) {
        this.themeCode = themeCode;
        return this;
    }

    @Override
    public String toString() {
        return "BlogInfo{id=" + id + ", bloggerName='" + bloggerName + "', themeCode='" + themeCode + "'}";
    }
}
