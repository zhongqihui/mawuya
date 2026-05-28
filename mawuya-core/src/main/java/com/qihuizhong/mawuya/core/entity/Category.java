/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 博客分类实体（与 category_info 表对应）
 *
 * @author zqh
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private String categoryName;
    /** 该分类下的文章列表（仅在前台展示分类详情时填充） */
    private List<ArticleInfo> arts;
    /** 该分类下的文章数 */
    private int artSize;

    public Category() {
    }

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public static Category of(String categoryName) {
        return new Category(categoryName);
    }

    public Integer getSn() {
        return sn;
    }

    public Category setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Category setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return this;
    }

    public List<ArticleInfo> getArts() {
        return arts;
    }

    public Category setArts(List<ArticleInfo> arts) {
        this.arts = arts;
        return this;
    }

    public int getArtSize() {
        return artSize;
    }

    public Category setArtSize(int artSize) {
        this.artSize = artSize;
        return this;
    }

    @Override
    public String toString() {
        return "Category{sn=" + sn + ", categoryName='" + categoryName + "', artSize=" + artSize + '}';
    }
}
