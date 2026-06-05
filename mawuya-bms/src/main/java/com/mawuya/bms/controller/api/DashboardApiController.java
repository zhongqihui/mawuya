/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.controller.api;

import com.mawuya.bms.dto.response.DashboardDataResponse;
import com.mawuya.core.common.BaseResponse;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.mapper.ArticleInfoMapper;
import com.mawuya.core.mapper.CategoryMapper;
import com.mawuya.core.mapper.DashboardMapper;
import com.mawuya.core.mapper.ReviewInfoMapper;
import com.mawuya.core.mapper.TagMapper;
import com.mawuya.core.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BMS 首页数据看板 JSON API。
 *
 * <p>路由 {@code GET /bms/api/dashboard/data} 一次性返回所有看板数据，
 * 方便前端单次请求渲染整页（避免多接口带来的视觉抖动）。返回结构见
 * {@link DashboardDataResponse}。</p>
 *
 * <p>遵循阿里规约：
 * <ul>
 *   <li>日期处理统一使用 {@link LocalDate} + {@link DateTimeFormatter}（线程安全），不再使用
 *       {@code SimpleDateFormat} / {@code Calendar}（线程不安全且已被 Java 8+ 标记为遗留 API）。</li>
 *   <li>HashMap / ArrayList 显式指定初始容量，避免扩容抖动。</li>
 *   <li>魔法值统一抽常量。</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/dashboard")
public class DashboardApiController {

    private static final Logger log = LoggerFactory.getLogger(DashboardApiController.class);

    /** 趋势图覆盖天数（近 N 天） */
    private static final int TREND_DAYS = 14;
    /** 累计 PV 兜底统计窗口 */
    private static final int PV_TOTAL_WINDOW_DAYS = 365;
    /** "近 7 天新增"窗口 */
    private static final int RECENT_DAYS_FOR_NEW = 7;
    /** TOP 榜数量（文章 / IP） */
    private static final int TOP_LIMIT = 5;
    /** 健康度评分各因子的最高分 */
    private static final int HEALTH_FACTOR_MAX = 25;
    /** 内容存量满分阈值（文章数） */
    private static final double HEALTH_THRESHOLD_ARTICLES = 30.0;
    /** 更新活跃度满分阈值（近 7 天新增） */
    private static final double HEALTH_THRESHOLD_RECENT = 3.0;
    /** 互动健康度满分阈值（评论数） */
    private static final double HEALTH_THRESHOLD_REVIEWS = 50.0;
    /** 访客覆盖度满分阈值（UV） */
    private static final double HEALTH_THRESHOLD_UV = 100.0;
    /** 未分类 ≥ 该值时扣分 */
    private static final int HEALTH_PENALTY_UNCATEGORIZED = 5;
    /** 点赞率不足该比例扣分 */
    private static final double HEALTH_PRAISE_RATIO_THRESHOLD = 0.8;
    /** 健康度等级阈值 */
    private static final int LEVEL_EXCELLENT = 90;
    private static final int LEVEL_GREAT     = 75;
    private static final int LEVEL_GOOD      = 60;
    private static final int LEVEL_FAIR      = 40;
    /** Map 初始容量（针对 7~16 个 key 的常用 LinkedHashMap） */
    private static final int MAP_INIT_CAP_SMALL = 16;
    private static final int MAP_INIT_CAP_TINY  = 8;
    /** yyyy-MM-dd 日期格式（不可变线程安全） */
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired private ArticleInfoMapper articleInfoMapper;
    @Autowired private CategoryMapper    categoryMapper;
    @Autowired private TagMapper         tagMapper;
    @Autowired private ReviewInfoMapper  reviewInfoMapper;
    @Autowired private DashboardMapper   dashboardMapper;

    @GetMapping("data")
    public BaseResponse<DashboardDataResponse> data() {
        try {
            DashboardDataResponse res = new DashboardDataResponse();

            // ---------- 1. 内容数据 ----------
            int articles      = articleInfoMapper.selectCount(new HashMap<>(MAP_INIT_CAP_TINY));
            int articlesNew7d = dashboardMapper.countArticlesInLastDays(RECENT_DAYS_FOR_NEW);
            int categories    = categoryMapper.selectCount(new HashMap<>(MAP_INIT_CAP_TINY));
            int tags          = tagMapper.selectCount(new HashMap<>(MAP_INIT_CAP_TINY));
            long contentChars = dashboardMapper.sumArticleContentLength();

            Map<String, Object> content = new LinkedHashMap<>(MAP_INIT_CAP_TINY);
            content.put("articles",      articles);
            content.put("articlesNew7d", articlesNew7d);
            content.put("categories",    categories);
            content.put("tags",          tags);
            content.put("contentChars",  contentChars);
            res.setContent(content);

            // ---------- 2. 互动数据 ----------
            int rPending  = reviewInfoMapper.countByStatus(ReviewService.STATUS_PENDING);
            int rApproved = reviewInfoMapper.countByStatus(ReviewService.STATUS_APPROVED);
            int rRejected = reviewInfoMapper.countByStatus(ReviewService.STATUS_REJECTED);
            int rTotal    = rPending + rApproved + rRejected;
            long praise = dashboardMapper.sumPraiseNum();
            long tease  = dashboardMapper.sumTeaseNum();
            double praiseRatio = (praise + tease) == 0 ? 0d
                    : Math.round(praise * 1000.0 / (praise + tease)) / 10.0;

            Map<String, Object> interact = new LinkedHashMap<>(MAP_INIT_CAP_SMALL);
            interact.put("reviews",     rTotal);
            interact.put("pending",     rPending);
            interact.put("approved",    rApproved);
            interact.put("rejected",    rRejected);
            interact.put("praise",      praise);
            interact.put("tease",       tease);
            interact.put("praiseRatio", praiseRatio);
            res.setInteract(interact);

            // ---------- 3. 流量数据 ----------
            int pvToday  = dashboardMapper.pvToday();
            int uvToday  = dashboardMapper.uvToday();
            int uvTotal  = dashboardMapper.uvTotal();
            long readTotal = dashboardMapper.sumReadNum();
            int pvTotal = sumPvFromTrend(PV_TOTAL_WINDOW_DAYS);

            Map<String, Object> traffic = new LinkedHashMap<>(MAP_INIT_CAP_TINY);
            traffic.put("pvToday",   pvToday);
            traffic.put("uvToday",   uvToday);
            traffic.put("pvTotal",   pvTotal);
            traffic.put("uvTotal",   uvTotal);
            traffic.put("readTotal", readTotal);
            res.setTraffic(traffic);

            // ---------- 4. 近 14 天趋势 ----------
            res.setTrend(buildTrend(TREND_DAYS));

            // ---------- 5. TOP 文章 / TOP IP ----------
            List<Map<String, Object>> topArticles = dashboardMapper.topArticles(TOP_LIMIT);
            res.setTopArticles(topArticles == null ? new ArrayList<>(0) : topArticles);
            List<Map<String, Object>> topIps = dashboardMapper.topIpsToday(TOP_LIMIT);
            res.setTopIpsToday(topIps == null ? new ArrayList<>(0) : topIps);

            // ---------- 6. 待办提醒 ----------
            int uncategorized = dashboardMapper.countUncategorizedArticles();
            Map<String, Object> todo = new LinkedHashMap<>(MAP_INIT_CAP_TINY);
            todo.put("pendingReviews", rPending);
            todo.put("uncategorized",  uncategorized);
            res.setTodo(todo);

            // ---------- 7. 博客健康度评分 ----------
            res.setHealth(buildHealth(articles, articlesNew7d, rTotal, praise, tease, uvTotal, uncategorized));

            return BaseResponse.success(res);
        } catch (BusinessException be) {
            // 业务异常透传由全局异常处理器处理，避免被下面的 Exception 吞掉
            throw be;
        } catch (Exception e) {
            log.error("[bms/api/dashboard] data assemble failed", e);
            throw new BusinessException("看板数据加载失败：" + e.getMessage());
        }
    }

    // ====================================================================
    // 私有工具方法
    // ====================================================================

    /** 通过 pvUvByDay 在大窗口（如 365 天）汇总，作为「累计 PV」的近似值。 */
    private int sumPvFromTrend(final int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<Map<String, Object>> rows = dashboardMapper.pvUvByDay(start.format(ISO_DATE), end.format(ISO_DATE));
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Map<String, Object> r : rows) {
            Object pv = r.get("pv");
            if (pv != null) {
                sum += ((Number) pv).intValue();
            }
        }
        return sum;
    }

    /** 构建近 N 天的 PV/UV 趋势图：按日期对齐补 0。 */
    private Map<String, Object> buildTrend(final int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);

        List<Map<String, Object>> rows = dashboardMapper.pvUvByDay(start.format(ISO_DATE), end.format(ISO_DATE));
        Map<String, int[]> dateMap = new HashMap<>(days * 2);
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Object dateObj = r.get("date");
                if (dateObj == null) {
                    continue;
                }
                String date = dateObj.toString();
                if (date.length() > 10) {
                    date = date.substring(0, 10);
                }
                int pv = r.get("pv") == null ? 0 : ((Number) r.get("pv")).intValue();
                int uv = r.get("uv") == null ? 0 : ((Number) r.get("uv")).intValue();
                dateMap.put(date, new int[]{pv, uv});
            }
        }

        List<String> dates  = new ArrayList<>(days);
        List<Integer> pvs   = new ArrayList<>(days);
        List<Integer> uvs   = new ArrayList<>(days);
        LocalDate cursor = start;
        for (int i = 0; i < days; i++) {
            String d = cursor.format(ISO_DATE);
            dates.add(d);
            int[] v = dateMap.get(d);
            pvs.add(v == null ? 0 : v[0]);
            uvs.add(v == null ? 0 : v[1]);
            cursor = cursor.plusDays(1L);
        }
        Map<String, Object> trend = new LinkedHashMap<>(MAP_INIT_CAP_TINY);
        trend.put("dates", dates);
        trend.put("pv",    pvs);
        trend.put("uv",    uvs);
        return trend;
    }

    /** 健康度评分：4 个 25 分维度 + 扣分。 */
    private Map<String, Object> buildHealth(final int articles, final int articlesNew7d,
                                            final int reviews,  final long praise,
                                            final long tease,   final int uvTotal,
                                            final int uncategorized) {
        double s1 = Math.min(HEALTH_FACTOR_MAX, articles      / HEALTH_THRESHOLD_ARTICLES * HEALTH_FACTOR_MAX);
        double s2 = Math.min(HEALTH_FACTOR_MAX, articlesNew7d / HEALTH_THRESHOLD_RECENT   * HEALTH_FACTOR_MAX);
        double s3 = Math.min(HEALTH_FACTOR_MAX, reviews       / HEALTH_THRESHOLD_REVIEWS  * HEALTH_FACTOR_MAX);
        double s4 = Math.min(HEALTH_FACTOR_MAX, uvTotal       / HEALTH_THRESHOLD_UV       * HEALTH_FACTOR_MAX);
        double penalty = 0d;
        if (uncategorized >= HEALTH_PENALTY_UNCATEGORIZED) {
            penalty += HEALTH_PENALTY_UNCATEGORIZED;
        }
        double praiseRatio = (praise + tease) == 0 ? 1.0 : (double) praise / (praise + tease);
        if (praiseRatio < HEALTH_PRAISE_RATIO_THRESHOLD) {
            penalty += HEALTH_PENALTY_UNCATEGORIZED;
        }

        int score = (int) Math.round(Math.max(0, s1 + s2 + s3 + s4 - penalty));
        String level = scoreLevel(score);

        List<Map<String, Object>> factors = new ArrayList<>(4);
        factors.add(factor("内容存量",   (int) Math.round(s1), HEALTH_FACTOR_MAX));
        factors.add(factor("更新活跃度", (int) Math.round(s2), HEALTH_FACTOR_MAX));
        factors.add(factor("互动健康度", (int) Math.round(s3), HEALTH_FACTOR_MAX));
        factors.add(factor("访客覆盖度", (int) Math.round(s4), HEALTH_FACTOR_MAX));

        Map<String, Object> h = new LinkedHashMap<>(MAP_INIT_CAP_TINY);
        h.put("score",   score);
        h.put("level",   level);
        h.put("factors", Collections.unmodifiableList(factors));
        h.put("penalty", (int) penalty);
        return h;
    }

    private static String scoreLevel(final int score) {
        if (score >= LEVEL_EXCELLENT) {
            return "卓越";
        }
        if (score >= LEVEL_GREAT) {
            return "优秀";
        }
        if (score >= LEVEL_GOOD) {
            return "良好";
        }
        if (score >= LEVEL_FAIR) {
            return "一般";
        }
        return "待提升";
    }

    private static Map<String, Object> factor(final String name, final int value, final int weight) {
        Map<String, Object> m = new LinkedHashMap<>(4);
        m.put("name",   name);
        m.put("value",  value);
        m.put("weight", weight);
        return m;
    }
}
