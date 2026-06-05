/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.config;

import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.dataobject.ReviewDO;
import com.mawuya.core.dataobject.TagDO;
import com.mawuya.core.service.BlogStatsService;
import com.mawuya.core.service.SiteThemeService;
import org.springframework.beans.factory.annotation.Autowired;
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
@ControllerAdvice(basePackages = "com.mawuya.ams.controller")
public class GlobalModelAttributes {

    /** 侧边栏热门文章条数 */
    private static final int HOT_LIMIT = 5;
    /** 侧边栏最新评论条数 */
    private static final int RECENT_REVIEW_LIMIT = 5;
    /** 侧边栏标签云数量上限 */
    private static final int TAG_CLOUD_LIMIT = 30;

    private final BlogStatsService blogStatsService;
    private final SiteThemeService siteThemeService;

    @Autowired
    public GlobalModelAttributes(BlogStatsService blogStatsService, SiteThemeService siteThemeService) {
        this.blogStatsService = blogStatsService;
        this.siteThemeService = siteThemeService;
    }

    /**
     * 当前生效的 AMS 主题 code，由 BMS【主题切换】管理，落库于 blog_info.theme_code。
     * 视图通过 ${currentTheme} 写到 <html data-theme="..."> 上，配合主题 CSS 属性选择器命中皮肤。
     * 任意异常 / 空值 / 非法值 都会被 service 兜底为 'default'，前台永远不白屏。
     */
    @ModelAttribute("currentTheme")
    public String currentTheme() {
        return siteThemeService.getCurrentTheme();
    }

    @ModelAttribute("siteStats")
    public Map<String, Object> siteStats() {
        Map<String, Object> stats = new HashMap<>(8);
        stats.put("articleCount", blogStatsService.countArticles());
        stats.put("categoryCount", blogStatsService.countCategories());
        stats.put("tagCount", blogStatsService.countTags());
        stats.put("reviewCount", blogStatsService.countApprovedReviews());
        return stats;
    }

    @ModelAttribute("hotArticles")
    public List<ArticleDO> hotArticles() {
        return blogStatsService.listHotArticles(HOT_LIMIT);
    }

    @ModelAttribute("latestReviews")
    public List<ReviewDO> latestReviews() {
        return blogStatsService.listLatestReviews(RECENT_REVIEW_LIMIT);
    }

    @ModelAttribute("tagCloud")
    public List<TagDO> tagCloud() {
        List<TagDO> tags = blogStatsService.listTagCloud();
        if (tags.size() > TAG_CLOUD_LIMIT) {
            return tags.subList(0, TAG_CLOUD_LIMIT);
        }
        return tags;
    }
}
