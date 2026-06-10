/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import com.niudeyapi.mawuya.core.dataobject.ArticleStatusLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章状态流转日志 Mapper。
 *
 * @author 钟启辉
 */
@Mapper
public interface ArticleStatusLogMapper {

    /** 插入一条流转日志（change_time 由 DB 默认值 CURRENT_TIMESTAMP 填充）。 */
    int insert(ArticleStatusLogDO log);

    /** 列出某篇文章的全部状态变更，按时间倒序。 */
    List<ArticleStatusLogDO> selectByArticleSn(@Param("articleSn") Integer articleSn);
}
