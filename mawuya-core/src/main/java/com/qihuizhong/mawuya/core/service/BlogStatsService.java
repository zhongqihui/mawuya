/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import com.qihuizhong.mawuya.core.mapper.CategoryMapper;
import com.qihuizhong.mawuya.core.mapper.ReviewInfoMapper;
import com.qihuizhong.mawuya.core.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 站点统计 Service：统一为侧边栏与首页提供聚合数据。
 * <p>所有方法在异常时返回空值/0，避免拖垮主流程。</p>
 *
 * @author 钟启辉
 */
@Service
public class BlogStatsService {

    private final ArticleInfoMapper articleInfoMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ReviewInfoMapper reviewInfoMapper;

    @Autowired
    public BlogStatsService(ArticleInfoMapper articleInfoMapper,
                            CategoryMapper categoryMapper,
                            TagMapper tagMapper,
                            ReviewInfoMapper reviewInfoMapper) {
        this.articleInfoMapper = articleInfoMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.reviewInfoMapper = reviewInfoMapper;
    }

    public int getArticleCount() {
        try {
            return articleInfoMapper.selectCount(new HashMap<>());
        } catch (Exception e) {
            return 0;
        }
    }

    public int getCategoryCount() {
        try {
            return categoryMapper.selectCount(new HashMap<>());
        } catch (Exception e) {
            return 0;
        }
    }

    public int getTagCount() {
        try {
            return tagMapper.selectCount(new HashMap<>());
        } catch (Exception e) {
            return 0;
        }
    }

    public int getReviewCount() {
        try {
            // 站点对外暴露的"评论总数"仅计算已通过评论（与详情页可见数量一致）
            return reviewInfoMapper.countByStatus(ReviewService.STATUS_APPROVED);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<ArticleInfo> getHotArticles(int limit) {
        try {
            return articleInfoMapper.selectHotTopN(limit);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public List<ReviewInfo> getLatestReviews(int limit) {
        try {
            return reviewInfoMapper.selectLatestWithTitle(limit);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public List<Tag> getTagCloud() {
        try {
            return tagMapper.selectAllWithArtSize();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
