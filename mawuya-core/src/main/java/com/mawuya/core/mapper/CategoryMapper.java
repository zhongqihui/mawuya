/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.mapper;

import com.mawuya.core.dataobject.CategoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 博客分类 Mapper
 *
 * @author zqh
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO, Integer> {
}
