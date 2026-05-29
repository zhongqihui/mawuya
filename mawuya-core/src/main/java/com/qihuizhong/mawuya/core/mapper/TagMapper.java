/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.dataobject.TagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签 Mapper。
 *
 * @author 钟启辉
 */
@Mapper
public interface TagMapper extends BaseMapper<TagDO, Integer> {

    /**
     * 查询带文章计数的标签云（artSize 自动填充）
     */
    List<TagDO> selectAllWithArtSize();

    /**
     * 查询某文章的所有标签
     */
    List<TagDO> selectByArticleSn(@Param("articleSn") Integer articleSn);

    /**
     * 关联绑定：将一篇文章绑定到多个标签（先解绑、再绑定）
     */
    int unbindByArticle(@Param("articleSn") Integer articleSn);

    int bindArticleTag(@Param("articleSn") Integer articleSn, @Param("tagSn") Integer tagSn);

    /**
     * 按标签 sn 查文章 sn 列表
     */
    List<Integer> selectArticleSnByTag(@Param("tagSn") Integer tagSn);
}
