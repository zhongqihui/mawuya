/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.vo;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;

/**
 * 文章 VO（含单文章字段，与视图层相关）
 *
 * @author zqh
 */
public class ArticleVo extends Page<ArticleInfo> {

    private static final long serialVersionUID = 1L;

    private ArticleInfo article;

    public ArticleInfo getArticle() {
        return article;
    }

    public ArticleVo setArticle(ArticleInfo article) {
        this.article = article;
        return this;
    }
}
