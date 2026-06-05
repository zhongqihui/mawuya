/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.mapper;

import com.mawuya.core.dataobject.BlogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * blog_info 单例表 Mapper：仅暴露读单例 + 更新主题 code 两个能力，
 * 不沿用 BaseMapper（单例表无 list/page/count 语义）。
 *
 * @author zqh
 */
@Mapper
public interface BlogInfoMapper {

    /**
     * 读取单例行（id = 1）。若初始化脚本未执行可能返回 null，调用方需做兜底。
     */
    BlogDO selectSingleton();

    /**
     * 更新当前 AMS 主题 code。
     *
     * @param themeCode 主题 code（调用方需确保已通过白名单校验）
     * @return 受影响行数
     */
    int updateThemeCode(@Param("themeCode") String themeCode);
}
