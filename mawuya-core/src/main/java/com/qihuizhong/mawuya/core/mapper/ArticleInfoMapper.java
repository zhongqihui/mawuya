/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文章 Mapper
 *
 * @author 钟启辉
 */
@Mapper
public interface ArticleInfoMapper extends BaseMapper<ArticleInfo, Integer> {

    List<ArticleInfo> selectAllNoContent(Map<String, ?> map);

    ArticleInfo selectNextById(Integer id);

    ArticleInfo selectPrevById(Integer id);

    /**
     * 批量将 sns 的 readNum + 1。
     * <p>注意：sns 来源于服务端代码内部拼装（来自 Session 中已访问的文章 sn 集合），不会接收用户直接输入；
     * MyBatis ${} 在此场景下可控且不会引入注入。</p>
     */
    int updateBatchReadNum(@Param("value") String sns);

    int updateBatchCategorySn(@Param("value") String sns);

    int updatePictureUrl(ArticleInfo a);

    /**
     * 关键字搜索：在标题、摘要、正文中模糊匹配（不返回正文以减小流量）。
     * 入参 keyword 已经在 Service 层做了 SQL 通配符转义。
     */
    List<ArticleInfo> searchByKeyword(@Param("keyword") String keyword,
                                      @Param("start") Integer start,
                                      @Param("limit") Integer limit);

    int countByKeyword(@Param("keyword") String keyword);

    /**
     * 按 sn 列表批量查询（不带正文）
     */
    List<ArticleInfo> selectListBySnList(@Param("sns") List<Integer> sns);

    /**
     * 热门文章 Top N（按阅读数倒序）
     */
    List<ArticleInfo> selectHotTopN(@Param("limit") int limit);
}
