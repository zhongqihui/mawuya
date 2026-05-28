/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.LogInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 访客记录 Mapper
 *
 * @author zqh
 */
@Mapper
public interface LogInfoMapper extends BaseMapper<LogInfo, Integer> {

    int insertBatch(@Param("list") List<LogInfo> list);
}
