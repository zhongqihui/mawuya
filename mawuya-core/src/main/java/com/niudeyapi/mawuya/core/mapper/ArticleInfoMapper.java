/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文章 Mapper
 *
 * <p>所有列表 / 计数 / 搜索接口均支持 {@code status} 过滤：
 * <ul>
 *   <li>{@code 0} = 草稿</li>
 *   <li>{@code 1} = 已发布（AMS 对外可见）</li>
 *   <li>{@code 2} = 已撤回（可重新编辑，对外不可见）</li>
 * </ul>
 * AMS 端调用必须显式带 {@code status=1}；BMS 端可不带（看全部）或按 tab 传。
 *
 * @author 钟启辉
 */
@Mapper
public interface ArticleInfoMapper extends BaseMapper<ArticleDO, Integer> {

    List<ArticleDO> selectAllNoContent(Map<String, ?> map);

    /** 下一篇（已限定 status=1，避免跳到草稿/撤回） */
    ArticleDO selectNextById(Integer id);

    /** 上一篇（已限定 status=1） */
    ArticleDO selectPrevById(Integer id);

    /**
     * 批量将 sns 的 readNum + 1。
     * <p>注意：sns 来源于服务端代码内部拼装（来自 Session 中已访问的文章 sn 集合），不会接收用户直接输入；
     * MyBatis ${} 在此场景下可控且不会引入注入。</p>
     */
    int updateBatchReadNum(@Param("value") String sns);

    int updateBatchCategorySn(@Param("value") String sns);

    int updatePictureUrl(ArticleDO a);

    /**
     * 仅更新状态字段（草稿 / 发布 / 撤回 流转用），不影响业务字段。
     *
     * @param sn                       文章 sn
     * @param status                   目标状态
     * @param fillInsertTimeIfNull     是否在 insert_time 为 NULL 时补 NOW()（草稿首次发布需要）
     */
    int updateStatus(@Param("sn") Integer sn,
                     @Param("status") Integer status,
                     @Param("fillInsertTimeIfNull") boolean fillInsertTimeIfNull);

    /**
     * 关键字搜索：在标题、摘要、正文中模糊匹配（不返回正文以减小流量）。
     * keyword 已在 Service 层做了 SQL 通配符转义；status 可空（BMS 看全部）。
     */
    List<ArticleDO> searchByKeyword(@Param("keyword") String keyword,
                                      @Param("status") Integer status,
                                      @Param("start") Integer start,
                                      @Param("limit") Integer limit);

    int countByKeyword(@Param("keyword") String keyword,
                       @Param("status") Integer status);

    /**
     * 按 sn 列表批量查询（不带正文）。
     *
     * @param sns    sn 列表
     * @param status 过滤状态；AMS 端调用建议传 1
     */
    List<ArticleDO> selectListBySnList(@Param("sns") List<Integer> sns,
                                       @Param("status") Integer status);

    /**
     * 热门文章 Top N（按阅读数倒序）。
     *
     * @param status 过滤状态；AMS 端调用建议传 1
     */
    List<ArticleDO> selectHotTopN(@Param("limit") int limit,
                                  @Param("status") Integer status);
}
