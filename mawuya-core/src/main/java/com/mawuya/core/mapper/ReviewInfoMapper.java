/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.mapper;

import com.mawuya.core.dataobject.ReviewDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper。
 *
 * @author 钟启辉
 */
@Mapper
public interface ReviewInfoMapper extends BaseMapper<ReviewDO, Integer> {

    /**
     * 查询某篇文章下"已审核通过"的所有评论（按时间倒序）
     */
    List<ReviewDO> selectByArticleSn(@Param("articleSn") Integer articleSn);

    /**
     * 查询全站最新 N 条"已审核通过"评论（带文章标题，用于侧边栏）
     */
    List<ReviewDO> selectLatestWithTitle(@Param("limit") int limit);

    /**
     * 同步更新文章评论计数：article_info.review_num = (评论数，仅统计已通过)
     */
    int refreshArticleReviewNum(@Param("articleSn") Integer articleSn);

    /**
     * BMS 审批列表：按 status 过滤评论（status 为 null 表示全部），带 article_title。
     * 当 status=null 时按 (status ASC, date DESC) 排序，让"待审核(0)"自然排在最前。
     */
    List<ReviewDO> selectByStatusWithTitle(@Param("status") Integer status,
                                             @Param("offset") Integer offset,
                                             @Param("limit") Integer limit);

    /** 与 selectByStatusWithTitle 配套的总数（status 为 null 表示全部） */
    int countByStatus(@Param("status") Integer status);

    /** 仅更新审核状态（BMS approve/reject 用） */
    int updateStatus(@Param("sn") Integer sn, @Param("status") Integer status);
}
