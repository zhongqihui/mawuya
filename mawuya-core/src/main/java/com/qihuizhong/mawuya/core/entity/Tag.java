/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 文章标签（tag_info 表对应）。
 *
 * @author 钟启辉
 */
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private String tagName;
    private String tagColor;
    /** 该标签下的文章数（仅在标签云查询时填充） */
    private Integer artSize;

    public Tag() {
    }

    public Tag(String tagName) {
        this.tagName = tagName;
    }

    public Integer getSn() { return sn; }
    public Tag setSn(Integer sn) { this.sn = sn; return this; }

    public String getTagName() { return tagName; }
    public Tag setTagName(String tagName) { this.tagName = tagName; return this; }

    public String getTagColor() { return tagColor; }
    public Tag setTagColor(String tagColor) { this.tagColor = tagColor; return this; }

    public Integer getArtSize() { return artSize; }
    public Tag setArtSize(Integer artSize) { this.artSize = artSize; return this; }

    @Override
    public String toString() {
        return "Tag{sn=" + sn + ", tagName='" + tagName + "', tagColor='" + tagColor + "', artSize=" + artSize + '}';
    }
}
