/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 文章数据对象（与数据库表 {@code article_info} 对应）。
 *
 * <p>遵循《阿里巴巴 Java 开发手册》POJO 规约：
 * <ul>
 *   <li>类名以 {@code DO} 后缀标识"与库表对应"，放在 {@code dataobject} 包下；</li>
 *   <li>所有数值字段使用包装类型，避免拆箱 NPE；</li>
 *   <li>不在字段处赋默认值，由调用方显式赋值；</li>
 *   <li>提供 {@code toString()} 便于日志与排查（不输出大字段 articleContent）。</li>
 * </ul>
 *
 * @author 钟启辉
 */
public class ArticleDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文章主键 sn */
    private Integer sn;
    /** 文章归类 sn */
    private Integer categorySn;
    /** 阅读次数 */
    private Integer readNum;
    /** 评论次数 */
    private Integer reviewNum;
    /** 点赞次数 */
    private Integer praiseNum;
    /** 点踩次数 */
    private Integer teaseNum;
    /** 背景图片 url */
    private String pictureUrl;
    /** 文章标题 */
    private String articleTitle;
    /** 文章概要 */
    private String articleSummary;
    /** 文章内容 */
    private String articleContent;
    /** 文章插入时间（yyyy-MM-dd HH:mm:ss） */
    private String insertTime;
    /** 文章修改时间（yyyy-MM-dd HH:mm:ss） */
    private String updateTime;

    public ArticleDO() {
    }

    public Integer getSn() {
        return sn;
    }

    public ArticleDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public Integer getCategorySn() {
        return categorySn;
    }

    public ArticleDO setCategorySn(Integer categorySn) {
        this.categorySn = categorySn;
        return this;
    }

    public Integer getReadNum() {
        return readNum;
    }

    public ArticleDO setReadNum(Integer readNum) {
        this.readNum = readNum;
        return this;
    }

    public Integer getReviewNum() {
        return reviewNum;
    }

    public ArticleDO setReviewNum(Integer reviewNum) {
        this.reviewNum = reviewNum;
        return this;
    }

    public Integer getPraiseNum() {
        return praiseNum;
    }

    public ArticleDO setPraiseNum(Integer praiseNum) {
        this.praiseNum = praiseNum;
        return this;
    }

    public Integer getTeaseNum() {
        return teaseNum;
    }

    public ArticleDO setTeaseNum(Integer teaseNum) {
        this.teaseNum = teaseNum;
        return this;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public ArticleDO setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
        return this;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public ArticleDO setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
        return this;
    }

    public String getArticleSummary() {
        return articleSummary;
    }

    public ArticleDO setArticleSummary(String articleSummary) {
        this.articleSummary = articleSummary;
        return this;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public ArticleDO setArticleContent(String articleContent) {
        this.articleContent = articleContent;
        return this;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public ArticleDO setInsertTime(String insertTime) {
        this.insertTime = insertTime;
        return this;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public ArticleDO setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        return "ArticleDO{" +
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
