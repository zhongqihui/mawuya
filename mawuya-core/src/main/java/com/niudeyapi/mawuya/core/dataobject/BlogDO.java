/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 博客信息数据对象（与数据库表 {@code blog_info} 单例对应，固定 id = 1）。
 *
 * <p>承载站点级配置：博主名、AMS 当前皮肤主题 code 等。</p>
 *
 * @author 钟启辉
 */
public class BlogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单例主键，固定为 1 */
    private Integer id;
    /** 博主名 */
    private String bloggerName;
    /** AMS 当前主题 code：default / tech-dark / gradient-vivid / minimal-business */
    private String themeCode;

    public BlogDO() {
    }

    public Integer getId() {
        return id;
    }

    public BlogDO setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getBloggerName() {
        return bloggerName;
    }

    public BlogDO setBloggerName(String bloggerName) {
        this.bloggerName = bloggerName;
        return this;
    }

    public String getThemeCode() {
        return themeCode;
    }

    public BlogDO setThemeCode(String themeCode) {
        this.themeCode = themeCode;
        return this;
    }

    @Override
    public String toString() {
        return "BlogDO{id=" + id + ", bloggerName='" + bloggerName + "', themeCode='" + themeCode + "'}";
    }
}
