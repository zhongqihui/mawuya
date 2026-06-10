/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import com.niudeyapi.mawuya.core.dataobject.TagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 标签 Mapper。
 *
 * @author 钟启辉
 */
@Mapper
public interface TagMapper extends BaseMapper<TagDO, Integer> {

    /**
     * 查询带文章计数的标签云（artSize 自动填充）。
     * <p><strong>含全部状态的文章</strong>（草稿/已发布/已撤回），供 BMS 标签管理页
     * 「删除前确认」类决策使用。AMS 前台请改用 {@link #selectAllWithPublishedArtSize()}。</p>
     */
    List<TagDO> selectAllWithArtSize();

    /**
     * 查询带「已发布」文章计数的标签云（artSize 仅统计 article_info.status = 1）。
     * <p>AMS 右侧标签云专用：草稿/已撤回的文章不计入对外可见的标签热度。</p>
     */
    List<TagDO> selectAllWithPublishedArtSize();

    /** 查询某文章的所有标签。 */
    List<TagDO> selectByArticleSn(@Param("articleSn") Integer articleSn);

    /** 解绑某文章下的所有标签关联（不会因不存在而失败）。 */
    int unbindByArticle(@Param("articleSn") Integer articleSn);

    /** 解绑某标签下所有文章关联（标签删除时调用）。 */
    int unbindByTagSn(@Param("tagSn") Integer tagSn);

    /** 绑定一篇文章与一个标签（INSERT IGNORE 幂等）。 */
    int bindArticleTag(@Param("articleSn") Integer articleSn, @Param("tagSn") Integer tagSn);

    /** 按标签 sn 查文章 sn 列表（全部状态）。 */
    List<Integer> selectArticleSnByTag(@Param("tagSn") Integer tagSn);

    /**
     * 按标签 sn 查「已发布」文章 sn 列表。
     * <p>AMS 标签详情页（/tags/{sn}）专用，避免草稿/撤回稿对外曝光。</p>
     */
    List<Integer> selectPublishedArticleSnByTag(@Param("tagSn") Integer tagSn);

    /**
     * 按 tagName 精确查询，返回 0/1 条；用于唯一性校验与 getOrCreate 场景。
     * 不直接返回单条，是为了规避「无匹配」时 selectOne 抛 TooManyResultsException 的边界情况。
     */
    List<TagDO> selectByName(Map<String, ?> cond);
}
