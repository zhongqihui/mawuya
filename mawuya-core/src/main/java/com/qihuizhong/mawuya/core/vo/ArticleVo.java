/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.vo;

import com.qihuizhong.mawuya.core.dataobject.ArticleDO;

/**
 * 文章 VO（含单文章字段，与视图层相关）
 *
 * @author zqh
 */
public class ArticleVo extends Page<ArticleDO> {

    private static final long serialVersionUID = 1L;

    private ArticleDO article;

    public ArticleDO getArticle() {
        return article;
    }

    public ArticleVo setArticle(ArticleDO article) {
        this.article = article;
        return this;
    }
}
