/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * BMS Dashboard 聚合查询专用 Mapper。
 *
 * <p>所有 SQL 仅做 SELECT 聚合 / GROUP BY，不修改任何表；返回 Map 便于在 controller
 * 直接组装 JSON。</p>
 *
 * @author 钟启辉
 */
@Mapper
public interface DashboardMapper {

    /** 近 N 天发布的文章数 */
    int countArticlesInLastDays(@Param("days") int days);

    /** 当前未分类（category_sn=0）的文章数 */
    int countUncategorizedArticles();

    /** 全部文章正文字符总长度（粗略估计博主创作量） */
    long sumArticleContentLength();

    /** 文章 TOP N（按 read_num 倒序），返回 sn / title / readNum / reviewNum / praiseNum */
    List<Map<String, Object>> topArticles(@Param("limit") int limit);

    /**
     * 按"日期"统计访问 PV/UV，返回 [{date:'2026-05-22', pv:123, uv:45}, ...]
     * 使用 DATE(req_time) 分组，区间含起止；为保证日期连续性由 service 层做空日填补。
     */
    List<Map<String, Object>> pvUvByDay(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    /** 今日 TOP N 访问 IP（带次数） */
    List<Map<String, Object>> topIpsToday(@Param("limit") int limit);

    /** 今日 PV：log_info 中 DATE(req_time)=CURDATE() 的总数 */
    int pvToday();

    /** 今日 UV：log_info 中 DATE(req_time)=CURDATE() 的去重 IP 数 */
    int uvToday();

    /** 累计 UV（不分日期，去重 IP） */
    int uvTotal();

    /** 总点赞数 */
    long sumPraiseNum();

    /** 总踩数 */
    long sumTeaseNum();

    /** 总阅读数（来自 article_info.read_num；与 log_info 的 PV 不同） */
    long sumReadNum();
}
