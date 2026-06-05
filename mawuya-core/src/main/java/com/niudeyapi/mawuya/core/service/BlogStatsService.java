/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.dataobject.ReviewDO;
import com.niudeyapi.mawuya.core.dataobject.TagDO;
import com.niudeyapi.mawuya.core.mapper.ArticleInfoMapper;
import com.niudeyapi.mawuya.core.mapper.CategoryMapper;
import com.niudeyapi.mawuya.core.mapper.ReviewInfoMapper;
import com.niudeyapi.mawuya.core.mapper.TagMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 站点统计 Service：统一为侧边栏与首页提供聚合数据。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code count/list}（单值数值统计用 count，列表用 list）；
 * 所有方法异常时返回空值/0 兜底，避免拖垮主流程；同时通过 {@code log.warn} 留痕。</p>
 *
 * @author 钟启辉
 */
@Service
public class BlogStatsService {

    private static final Logger log = LoggerFactory.getLogger(BlogStatsService.class);

    /** 空查询条件 Map 的初始容量 */
    private static final int EMPTY_CONDITION_CAPACITY = 4;

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

    /** 文章总数。 */
    public int countArticles() {
        try {
            return articleInfoMapper.selectCount(new HashMap<>(EMPTY_CONDITION_CAPACITY));
        } catch (Exception e) {
            log.warn("[stats] countArticles failed: {}", e.getMessage());
            return 0;
        }
    }

    /** 分类总数。 */
    public int countCategories() {
        try {
            return categoryMapper.selectCount(new HashMap<>(EMPTY_CONDITION_CAPACITY));
        } catch (Exception e) {
            log.warn("[stats] countCategories failed: {}", e.getMessage());
            return 0;
        }
    }

    /** 标签总数。 */
    public int countTags() {
        try {
            return tagMapper.selectCount(new HashMap<>(EMPTY_CONDITION_CAPACITY));
        } catch (Exception e) {
            log.warn("[stats] countTags failed: {}", e.getMessage());
            return 0;
        }
    }

    /** 已通过评论总数（站点对外暴露的「评论总数」与详情页可见数量一致）。 */
    public int countApprovedReviews() {
        try {
            return reviewInfoMapper.countByStatus(ReviewService.STATUS_APPROVED);
        } catch (Exception e) {
            log.warn("[stats] countApprovedReviews failed: {}", e.getMessage());
            return 0;
        }
    }

    /** 列出热门文章。 */
    public List<ArticleDO> listHotArticles(int limit) {
        try {
            return articleInfoMapper.selectHotTopN(limit);
        } catch (Exception e) {
            log.warn("[stats] listHotArticles failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 列出最新评论（带文章标题）。 */
    public List<ReviewDO> listLatestReviews(int limit) {
        try {
            return reviewInfoMapper.selectLatestWithTitle(limit);
        } catch (Exception e) {
            log.warn("[stats] listLatestReviews failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 列出标签云（带文章计数）。 */
    public List<TagDO> listTagCloud() {
        try {
            return tagMapper.selectAllWithArtSize();
        } catch (Exception e) {
            log.warn("[stats] listTagCloud failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
