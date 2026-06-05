/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * 看板数据响应 DTO（{@code GET /bms/api/dashboard/data}）。
 *
 * <p>结构按业务分组：content / interact / traffic / trend / topArticles / topIpsToday / todo / health。
 * 字段过多，内层结构仍用 {@code Map<String,Object>} 承接 service 返回，避免再细分 100+ 个 DTO。
 * 前端契约稳定，通过 javadoc 描述每组字段含义。</p>
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDataResponse {

    /** 内容数据：articles / articlesNew7d / categories / tags / contentChars */
    private Map<String, Object> content;
    /** 互动数据：reviews / pending / approved / rejected / praise / tease / praiseRatio */
    private Map<String, Object> interact;
    /** 流量数据：pvToday / uvToday / pvTotal / uvTotal / readTotal */
    private Map<String, Object> traffic;
    /** 近 14 天 PV/UV 趋势：dates: [...] / pv: [...] / uv: [...] */
    private Map<String, Object> trend;
    /** TOP 5 文章 */
    private List<Map<String, Object>> topArticles;
    /** 今日 TOP 5 访客 IP */
    private List<Map<String, Object>> topIpsToday;
    /** 待办：pendingReviews / uncategorized */
    private Map<String, Object> todo;
    /** 健康度：score / level / factors / penalty */
    private Map<String, Object> health;

    public Map<String, Object> getContent() { return content; }
    public DashboardDataResponse setContent(Map<String, Object> content) { this.content = content; return this; }

    public Map<String, Object> getInteract() { return interact; }
    public DashboardDataResponse setInteract(Map<String, Object> interact) { this.interact = interact; return this; }

    public Map<String, Object> getTraffic() { return traffic; }
    public DashboardDataResponse setTraffic(Map<String, Object> traffic) { this.traffic = traffic; return this; }

    public Map<String, Object> getTrend() { return trend; }
    public DashboardDataResponse setTrend(Map<String, Object> trend) { this.trend = trend; return this; }

    public List<Map<String, Object>> getTopArticles() { return topArticles; }
    public DashboardDataResponse setTopArticles(List<Map<String, Object>> topArticles) {
        this.topArticles = topArticles; return this;
    }

    public List<Map<String, Object>> getTopIpsToday() { return topIpsToday; }
    public DashboardDataResponse setTopIpsToday(List<Map<String, Object>> topIpsToday) {
        this.topIpsToday = topIpsToday; return this;
    }

    public Map<String, Object> getTodo() { return todo; }
    public DashboardDataResponse setTodo(Map<String, Object> todo) { this.todo = todo; return this; }

    public Map<String, Object> getHealth() { return health; }
    public DashboardDataResponse setHealth(Map<String, Object> health) { this.health = health; return this; }
}
