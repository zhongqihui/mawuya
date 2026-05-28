/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.config;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.service.BlogStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局 Model 注入：所有视图自动获得 sidebar/header 共用的统计数据。
 *
 * <p>侧边栏、顶部菜单、统计数字（日志/分类/标签）会在 fts/* 所有页面用到，
 * 通过 @ControllerAdvice 统一注入，避免每个 controller 重复装配。</p>
 *
 * @author 钟启辉
 */
@ControllerAdvice(basePackages = "com.qihuizhong.mawuya.ams.controller")
public class GlobalModelAttributes {

    /** 侧边栏热门文章条数 */
    private static final int HOT_LIMIT = 5;
    /** 侧边栏最新评论条数 */
    private static final int RECENT_REVIEW_LIMIT = 5;
    /** 侧边栏标签云数量上限 */
    private static final int TAG_CLOUD_LIMIT = 30;

    private final BlogStatsService blogStatsService;

    @Autowired
    public GlobalModelAttributes(BlogStatsService blogStatsService) {
        this.blogStatsService = blogStatsService;
    }

    @ModelAttribute("siteStats")
    public Map<String, Object> siteStats() {
        Map<String, Object> stats = new HashMap<>(8);
        stats.put("articleCount", blogStatsService.getArticleCount());
        stats.put("categoryCount", blogStatsService.getCategoryCount());
        stats.put("tagCount", blogStatsService.getTagCount());
        stats.put("reviewCount", blogStatsService.getReviewCount());
        return stats;
    }

    @ModelAttribute("hotArticles")
    public List<ArticleInfo> hotArticles() {
        return blogStatsService.getHotArticles(HOT_LIMIT);
    }

    @ModelAttribute("latestReviews")
    public List<ReviewInfo> latestReviews() {
        return blogStatsService.getLatestReviews(RECENT_REVIEW_LIMIT);
    }

    @ModelAttribute("tagCloud")
    public List<Tag> tagCloud() {
        List<Tag> tags = blogStatsService.getTagCloud();
        if (tags.size() > TAG_CLOUD_LIMIT) {
            return tags.subList(0, TAG_CLOUD_LIMIT);
        }
        return tags;
    }
}
