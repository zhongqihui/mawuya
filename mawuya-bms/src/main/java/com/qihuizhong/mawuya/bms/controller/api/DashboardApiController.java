/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller.api;

import com.qihuizhong.mawuya.bms.dto.response.DashboardDataResponse;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import com.qihuizhong.mawuya.core.mapper.CategoryMapper;
import com.qihuizhong.mawuya.core.mapper.DashboardMapper;
import com.qihuizhong.mawuya.core.mapper.ReviewInfoMapper;
import com.qihuizhong.mawuya.core.mapper.TagMapper;
import com.qihuizhong.mawuya.core.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
            int articles      = articleInfoMapper.selectCount(new HashMap<>());
            int articlesNew7d = dashboardMapper.countArticlesInLastDays(7);
            int categories    = categoryMapper.selectCount(new HashMap<>());
            int tags          = tagMapper.selectCount(new HashMap<>());
            long contentChars = dashboardMapper.sumArticleContentLength();

            Map<String, Object> content = new LinkedHashMap<>();
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

            Map<String, Object> interact = new LinkedHashMap<>();
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

            Map<String, Object> traffic = new LinkedHashMap<>();
            traffic.put("pvToday",   pvToday);
            traffic.put("uvToday",   uvToday);
            traffic.put("pvTotal",   pvTotal);
            traffic.put("uvTotal",   uvTotal);
            traffic.put("readTotal", readTotal);
            res.setTraffic(traffic);

            // ---------- 4. 近 14 天趋势 ----------
            res.setTrend(buildTrend(TREND_DAYS));

            // ---------- 5. TOP 文章 / TOP IP ----------
            List<Map<String, Object>> topArticles = dashboardMapper.topArticles(5);
            res.setTopArticles(topArticles == null ? new ArrayList<>() : topArticles);
            List<Map<String, Object>> topIps = dashboardMapper.topIpsToday(5);
            res.setTopIpsToday(topIps == null ? new ArrayList<>() : topIps);

            // ---------- 6. 待办提醒 ----------
            int uncategorized = dashboardMapper.countUncategorizedArticles();
            Map<String, Object> todo = new LinkedHashMap<>();
            todo.put("pendingReviews", rPending);
            todo.put("uncategorized",  uncategorized);
            res.setTodo(todo);

            // ---------- 7. 博客健康度评分 ----------
            res.setHealth(buildHealth(articles, articlesNew7d, rTotal, praise, tease, uvTotal, uncategorized));

            return BaseResponse.success(res);
        } catch (Exception e) {
            log.error("[bms/api/dashboard] data assemble failed", e);
            throw new BusinessException("看板数据加载失败：" + e.getMessage());
        }
    }

    // ----------------- 私有工具 -----------------

    /** 通过 pvUvByDay 在大窗口（如 365 天）汇总，作为"累计 PV"的近似 */
    private int sumPvFromTrend(int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String endDate = fmt.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        String startDate = fmt.format(cal.getTime());
        List<Map<String, Object>> rows = dashboardMapper.pvUvByDay(startDate, endDate);
        int sum = 0;
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Object pv = r.get("pv");
                if (pv != null) sum += ((Number) pv).intValue();
            }
        }
        return sum;
    }

    private Map<String, Object> buildTrend(int days) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String endDate = fmt.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        String startDate = fmt.format(cal.getTime());

        List<Map<String, Object>> rows = dashboardMapper.pvUvByDay(startDate, endDate);
        Map<String, int[]> dateMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Object dateObj = r.get("date");
                String date = dateObj == null ? null : dateObj.toString();
                if (date == null) continue;
                if (date.length() > 10) date = date.substring(0, 10);
                int pv = r.get("pv") == null ? 0 : ((Number) r.get("pv")).intValue();
                int uv = r.get("uv") == null ? 0 : ((Number) r.get("uv")).intValue();
                dateMap.put(date, new int[]{pv, uv});
            }
        }
        List<String> dates = new ArrayList<>(days);
        List<Integer> pvs = new ArrayList<>(days);
        List<Integer> uvs = new ArrayList<>(days);
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        for (int i = 0; i < days; i++) {
            String d = fmt.format(cal.getTime());
            dates.add(d);
            int[] v = dateMap.get(d);
            pvs.add(v == null ? 0 : v[0]);
            uvs.add(v == null ? 0 : v[1]);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("dates", dates);
        trend.put("pv",    pvs);
        trend.put("uv",    uvs);
        return trend;
    }

    private Map<String, Object> buildHealth(int articles, int articlesNew7d,
                                            int reviews, long praise, long tease,
                                            int uvTotal, int uncategorized) {
        double s1 = Math.min(25.0, articles      / 30.0  * 25.0);
        double s2 = Math.min(25.0, articlesNew7d / 3.0   * 25.0);
        double s3 = Math.min(25.0, reviews       / 50.0  * 25.0);
        double s4 = Math.min(25.0, uvTotal       / 100.0 * 25.0);
        double penalty = 0;
        if (uncategorized >= 5) penalty += 5;
        double praiseRatio = (praise + tease) == 0 ? 1.0 : (double) praise / (praise + tease);
        if (praiseRatio < 0.8) penalty += 5;

        int score = (int) Math.round(Math.max(0, s1 + s2 + s3 + s4 - penalty));
        String level;
        if (score >= 90)      level = "卓越";
        else if (score >= 75) level = "优秀";
        else if (score >= 60) level = "良好";
        else if (score >= 40) level = "一般";
        else                  level = "待提升";

        List<Map<String, Object>> factors = new ArrayList<>();
        factors.add(factor("内容存量",    (int) Math.round(s1), 25));
        factors.add(factor("更新活跃度",  (int) Math.round(s2), 25));
        factors.add(factor("互动健康度",  (int) Math.round(s3), 25));
        factors.add(factor("访客覆盖度",  (int) Math.round(s4), 25));

        Map<String, Object> h = new LinkedHashMap<>();
        h.put("score",   score);
        h.put("level",   level);
        h.put("factors", factors);
        h.put("penalty", (int) penalty);
        return h;
    }

    private static Map<String, Object> factor(String name, int value, int weight) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name",   name);
        m.put("value",  value);
        m.put("weight", weight);
        return m;
    }
}
