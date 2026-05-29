/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 文章新增请求 DTO（{@code POST /bms/api/article/create}）。
 *
 * @author 钟启辉
 */
public class ArticleCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长 200 字符")
    private String articleTitle;

    /** 分类 sn（0 表示未分类，可空） */
    private Integer categorySn;

    @Size(max = 1000, message = "摘要最长 1000 字符")
    private String articleSummary;

    @NotBlank(message = "正文不能为空")
    private String articleContent;

    public String getArticleTitle() { return articleTitle; }
    public ArticleCreateRequest setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; return this; }

    public Integer getCategorySn() { return categorySn; }
    public ArticleCreateRequest setCategorySn(Integer categorySn) { this.categorySn = categorySn; return this; }

    public String getArticleSummary() { return articleSummary; }
    public ArticleCreateRequest setArticleSummary(String articleSummary) { this.articleSummary = articleSummary; return this; }

    public String getArticleContent() { return articleContent; }
    public ArticleCreateRequest setArticleContent(String articleContent) { this.articleContent = articleContent; return this; }
}
