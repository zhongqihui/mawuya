/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.dataobject;

import java.io.Serializable;
import java.util.List;

/**
 * 博客分类数据对象（与数据库表 {@code category_info} 对应）。
 *
 * <p>{@code arts} / {@code artSize} 为非表字段，仅在前台展示分类详情时由 service 层填充。</p>
 *
 * @author 钟启辉
 */
public class CategoryDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类主键 sn */
    private Integer sn;
    /** 分类名 */
    private String categoryName;
    /** 该分类下的文章列表（仅在前台展示分类详情时填充） */
    private List<ArticleDO> arts;
    /** 该分类下的文章数 */
    private Integer artSize;

    public CategoryDO() {
    }

    public CategoryDO(String categoryName) {
        this.categoryName = categoryName;
    }

    public static CategoryDO of(String categoryName) {
        return new CategoryDO(categoryName);
    }

    public Integer getSn() {
        return sn;
    }

    public CategoryDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public CategoryDO setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return this;
    }

    public List<ArticleDO> getArts() {
        return arts;
    }

    public CategoryDO setArts(List<ArticleDO> arts) {
        this.arts = arts;
        return this;
    }

    public Integer getArtSize() {
        return artSize;
    }

    public CategoryDO setArtSize(Integer artSize) {
        this.artSize = artSize;
        return this;
    }

    @Override
    public String toString() {
        return "CategoryDO{sn=" + sn + ", categoryName='" + categoryName + "', artSize=" + artSize + '}';
    }
}
