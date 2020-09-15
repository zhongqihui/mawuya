/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2020 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.dto;

import com.qihuizhong.mawuya.core.dto.base.BaseDTO;

import java.util.List;

/**
 * 博文、文章的DTO对象
 */
public class ArticleInfoDTO extends BaseDTO {

    private static final long serialVersionUID = -1521506050136034083L;

    /**
     * 文章主键sn
     */
    private String sn;

    /**
     * 文章归类sn
     */
    private String categorySn;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章概要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String context;

    /**
     * 背景图片url
     */
    private String cover;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 阅读次数
     */
    private int readCount;

    /**
     * 评论次数
     */
    private int commentCount;

    /**
     * 赞次数
     */
    private int likeCount;

    /**
     * 踩次数
     */
    private int teaseCount;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCategorySn() {
        return categorySn;
    }

    public void setCategorySn(String categorySn) {
        this.categorySn = categorySn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getTeaseCount() {
        return teaseCount;
    }

    public void setTeaseCount(int teaseCount) {
        this.teaseCount = teaseCount;
    }
}
