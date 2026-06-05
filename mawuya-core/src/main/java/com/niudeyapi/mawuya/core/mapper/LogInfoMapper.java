/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import com.niudeyapi.mawuya.core.dataobject.LogDO;
import com.niudeyapi.mawuya.core.vo.LogInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 访客记录 Mapper
 *
 * @author zqh
 */
@Mapper
public interface LogInfoMapper extends BaseMapper<LogDO, Integer> {

    int insertBatch(@Param("list") List<LogDO> list);

    /** 多条件 + 时间范围 + 排序的分页查询。query 内字段已由 service 收敛。 */
    List<LogDO> selectByConditionPage(@Param("q") LogInfoQuery query);

    /** 与 selectByConditionPage 配套的总数。 */
    int countByCondition(@Param("q") LogInfoQuery query);
}
