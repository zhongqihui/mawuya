/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 评论数据对象（与数据库表 {@code review_info} 对应）。
 *
 * @author 钟启辉
 */
public class ReviewDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论主键 sn */
    private Integer sn;
    /** 关联文章 sn */
    private Integer articleSn;
    /** 父评论 sn（顶级评论为 0） */
    private Integer psn;
    /** 子评论关联 sn */
    private Integer csn;
    /** 点赞次数 */
    private Integer praiseNum;
    /** 点踩次数 */
    private Integer teaseNum;
    /** 评论人昵称 */
    private String reviewName;
    /** 评论内容 */
    private String reviewContent;
    /** 评论时间（yyyy-MM-dd HH:mm:ss） */
    private String reviewDate;
    /**
     * 审核状态：
     * <ul>
     *   <li>0 = 待审核（PENDING，新提交默认值）</li>
     *   <li>1 = 已通过（APPROVED，AMS 前台仅展示该状态的评论）</li>
     *   <li>2 = 已拒绝（REJECTED，前台不展示，BMS 中可见以便复核）</li>
     * </ul>
     */
    private Integer reviewStatus;
    /** 关联文章标题（仅在「最新评论」「BMS 审批列表」展示时填充） */
    private String articleTitle;

    public ReviewDO() {
    }

    public Integer getSn() {
        return sn;
    }

    public ReviewDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public Integer getArticleSn() {
        return articleSn;
    }

    public ReviewDO setArticleSn(Integer articleSn) {
        this.articleSn = articleSn;
        return this;
    }

    public Integer getPsn() {
        return psn;
    }

    public ReviewDO setPsn(Integer psn) {
        this.psn = psn;
        return this;
    }

    public Integer getCsn() {
        return csn;
    }

    public ReviewDO setCsn(Integer csn) {
        this.csn = csn;
        return this;
    }

    public Integer getPraiseNum() {
        return praiseNum;
    }

    public ReviewDO setPraiseNum(Integer praiseNum) {
        this.praiseNum = praiseNum;
        return this;
    }

    public Integer getTeaseNum() {
        return teaseNum;
    }

    public ReviewDO setTeaseNum(Integer teaseNum) {
        this.teaseNum = teaseNum;
        return this;
    }

    public String getReviewName() {
        return reviewName;
    }

    public ReviewDO setReviewName(String reviewName) {
        this.reviewName = reviewName;
        return this;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public ReviewDO setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
        return this;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public ReviewDO setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
        return this;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public ReviewDO setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
        return this;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public ReviewDO setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
        return this;
    }

    @Override
    public String toString() {
        return "ReviewDO{" +
                "sn=" + sn +
                ", articleSn=" + articleSn +
                ", psn=" + psn +
                ", csn=" + csn +
                ", praiseNum=" + praiseNum +
                ", teaseNum=" + teaseNum +
                ", reviewName='" + reviewName + '\'' +
                ", reviewDate='" + reviewDate + '\'' +
                ", reviewStatus=" + reviewStatus +
                '}';
    }
}
