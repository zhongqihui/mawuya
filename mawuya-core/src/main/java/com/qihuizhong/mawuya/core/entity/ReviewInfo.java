/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 评论（review_info 表对应）。
 *
 * @author 钟启辉
 */
public class ReviewInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private Integer articleSn;
    private Integer psn;
    private Integer csn;
    private int praiseNum;
    private int teaseNum;
    private String reviewName;
    private String reviewContent;
    private String reviewDate;
    /** 关联文章标题（仅在「最新评论」展示时填充） */
    private String articleTitle;

    public ReviewInfo() {
    }

    public Integer getSn() { return sn; }
    public ReviewInfo setSn(Integer sn) { this.sn = sn; return this; }

    public Integer getArticleSn() { return articleSn; }
    public ReviewInfo setArticleSn(Integer articleSn) { this.articleSn = articleSn; return this; }

    public Integer getPsn() { return psn; }
    public ReviewInfo setPsn(Integer psn) { this.psn = psn; return this; }

    public Integer getCsn() { return csn; }
    public ReviewInfo setCsn(Integer csn) { this.csn = csn; return this; }

    public int getPraiseNum() { return praiseNum; }
    public ReviewInfo setPraiseNum(int praiseNum) { this.praiseNum = praiseNum; return this; }

    public int getTeaseNum() { return teaseNum; }
    public ReviewInfo setTeaseNum(int teaseNum) { this.teaseNum = teaseNum; return this; }

    public String getReviewName() { return reviewName; }
    public ReviewInfo setReviewName(String reviewName) { this.reviewName = reviewName; return this; }

    public String getReviewContent() { return reviewContent; }
    public ReviewInfo setReviewContent(String reviewContent) { this.reviewContent = reviewContent; return this; }

    public String getReviewDate() { return reviewDate; }
    public ReviewInfo setReviewDate(String reviewDate) { this.reviewDate = reviewDate; return this; }

    public String getArticleTitle() { return articleTitle; }
    public ReviewInfo setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; return this; }
}
