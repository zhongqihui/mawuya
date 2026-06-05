/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 文章标签数据对象（与数据库表 {@code tag_info} 对应）。
 *
 * @author 钟启辉
 */
public class TagDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签主键 sn */
    private Integer sn;
    /** 标签名 */
    private String tagName;
    /** 标签配色（CSS 颜色值） */
    private String tagColor;
    /** 该标签下的文章数（仅在标签云查询时填充） */
    private Integer artSize;

    public TagDO() {
    }

    public TagDO(String tagName) {
        this.tagName = tagName;
    }

    public Integer getSn() {
        return sn;
    }

    public TagDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getTagName() {
        return tagName;
    }

    public TagDO setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public String getTagColor() {
        return tagColor;
    }

    public TagDO setTagColor(String tagColor) {
        this.tagColor = tagColor;
        return this;
    }

    public Integer getArtSize() {
        return artSize;
    }

    public TagDO setArtSize(Integer artSize) {
        this.artSize = artSize;
        return this;
    }

    @Override
    public String toString() {
        return "TagDO{sn=" + sn + ", tagName='" + tagName + "', tagColor='" + tagColor
                + "', artSize=" + artSize + '}';
    }
}
