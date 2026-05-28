/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 文章实体（与 article_info 表对应）
 *
 * @author 钟启辉
 */
public class ArticleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文章主键 sn */
    private Integer sn;
    /** 文章归类 sn */
    private int categorySn;
    /** 阅读次数 */
    private int readNum;
    /** 评论次数 */
    private int reviewNum;
    /** 赞次数 */
    private int praiseNum;
    /** 踩次数 */
    private int teaseNum;
    /** 背景图片 url */
    private String pictureUrl;
    /** 文章标题 */
    private String articleTitle;
    /** 文章概要 */
    private String articleSummary;
    /** 文章内容 */
    private String articleContent;
    /** 文章插入时间 */
    private String insertTime;
    /** 文章修改时间 */
    private String updateTime;

    public ArticleInfo() {
    }

    public Integer getSn() {
        return sn;
    }

    public ArticleInfo setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public int getCategorySn() {
        return categorySn;
    }

    public ArticleInfo setCategorySn(int categorySn) {
        this.categorySn = categorySn;
        return this;
    }

    public int getReadNum() {
        return readNum;
    }

    public ArticleInfo setReadNum(int readNum) {
        this.readNum = readNum;
        return this;
    }

    public int getReviewNum() {
        return reviewNum;
    }

    public ArticleInfo setReviewNum(int reviewNum) {
        this.reviewNum = reviewNum;
        return this;
    }

    public int getPraiseNum() {
        return praiseNum;
    }

    public ArticleInfo setPraiseNum(int praiseNum) {
        this.praiseNum = praiseNum;
        return this;
    }

    public int getTeaseNum() {
        return teaseNum;
    }

    public ArticleInfo setTeaseNum(int teaseNum) {
        this.teaseNum = teaseNum;
        return this;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public ArticleInfo setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
        return this;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public ArticleInfo setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
        return this;
    }

    public String getArticleSummary() {
        return articleSummary;
    }

    public ArticleInfo setArticleSummary(String articleSummary) {
        this.articleSummary = articleSummary;
        return this;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public ArticleInfo setArticleContent(String articleContent) {
        this.articleContent = articleContent;
        return this;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public ArticleInfo setInsertTime(String insertTime) {
        this.insertTime = insertTime;
        return this;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public ArticleInfo setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        return "ArticleInfo{" +
                "sn=" + sn +
                ", categorySn=" + categorySn +
                ", readNum=" + readNum +
                ", reviewNum=" + reviewNum +
                ", praiseNum=" + praiseNum +
                ", teaseNum=" + teaseNum +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", articleTitle='" + articleTitle + '\'' +
                ", articleSummary='" + articleSummary + '\'' +
                ", insertTime='" + insertTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
