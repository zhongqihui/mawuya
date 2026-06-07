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

    /**
     * 统计 req_time 早于指定时间字符串的日志条数。
     * <p>req_time 是字符串 {@code yyyy-MM-dd HH:mm:ss.SSS}，其字典序与时间序一致，
     * 因此用字符串比较即可，并能命中 {@code idx_log_req_time} 索引。</p>
     *
     * @param reqTimeBefore 截止时间（含格式 {@code yyyy-MM-dd HH:mm:ss}）
     * @return 命中条数
     */
    int countOlderThan(@Param("reqTimeBefore") String reqTimeBefore);

    /**
     * 删除 req_time 早于指定时间字符串的日志，单次删除上限由 limit 控制。
     * <p>必须分批（带 LIMIT）执行，避免一次删大量行造成长事务、binlog 膨胀、
     * 主从延迟、行锁升级等问题。</p>
     *
     * @param reqTimeBefore 截止时间（不含），早于此值的将被删除
     * @param limit         单批最大删除条数（≥1）
     * @return 实际删除条数
     */
    int deleteOlderThan(@Param("reqTimeBefore") String reqTimeBefore,
                        @Param("limit") int limit);
}
