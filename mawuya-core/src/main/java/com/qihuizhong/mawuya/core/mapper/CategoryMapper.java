/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 博客分类 Mapper
 *
 * @author zqh
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category, Integer> {
}
