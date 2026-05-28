/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper。
 *
 * @author 钟启辉
 */
@Mapper
public interface ReviewInfoMapper extends BaseMapper<ReviewInfo, Integer> {

    /**
     * 查询某篇文章下的所有顶层评论（按时间倒序）
     */
    List<ReviewInfo> selectByArticleSn(@Param("articleSn") Integer articleSn);

    /**
     * 查询全站最新 N 条评论（带文章标题，用于侧边栏）
     */
    List<ReviewInfo> selectLatestWithTitle(@Param("limit") int limit);

    /**
     * 同步更新文章评论计数：article_info.review_num = (评论数)
     */
    int refreshArticleReviewNum(@Param("articleSn") Integer articleSn);
}
