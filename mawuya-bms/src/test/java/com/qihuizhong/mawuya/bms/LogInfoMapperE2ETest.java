/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms;

import com.qihuizhong.mawuya.core.entity.LogInfo;
import com.qihuizhong.mawuya.core.mapper.LogInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 访客日志 Mapper 端到端测试。
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@DisplayName("E2E-4：LogInfoMapper 单条与批量插入")
class LogInfoMapperE2ETest {

    @Autowired
    private LogInfoMapper logInfoMapper;

    private LogInfo build(String ip) {
        return new LogInfo()
                .setIpAddr(ip)
                .setCountry("CN")
                .setProvince("Guangdong")
                .setCity("Shenzhen")
                .setArea("Nanshan")
                .setDetailPosition("Tencent Building")
                .setIsp("CT")
                .setTryTimes(0)
                .setReqTime("2026-05-28 11:00:00")
                .setRespTime("2026-05-28 11:00:00")
                .setConsumeTime("12ms")
                .setReqUrl("/")
                .setReqMethod("GET")
                .setParams("")
                .setBrowser("Mozilla/5.0")
                .setRespStatus("0")
                .setExceptMessage(null);
    }

    @Test
    @DisplayName("insert 单条访客日志成功")
    void shouldInsertSingle() {
        int rows = logInfoMapper.insert(build("10.0.0.1"));
        assertThat(rows).isEqualTo(1);

        Integer cnt = logInfoMapper.selectCount(new HashMap<>());
        assertThat(cnt).isPositive();
    }

    @Test
    @DisplayName("insertBatch 批量插入访客日志")
    void shouldInsertBatch() {
        Integer before = logInfoMapper.selectCount(new HashMap<>());
        List<LogInfo> batch = Arrays.asList(
                build("10.0.0.2"), build("10.0.0.3"), build("10.0.0.4"));
        int rows = logInfoMapper.insertBatch(batch);
        assertThat(rows).isEqualTo(3);

        Integer after = logInfoMapper.selectCount(new HashMap<>());
        assertThat(after - before).isEqualTo(3);
    }

    @Test
    @DisplayName("selectByPage 分页参数生效")
    void shouldPaginate() {
        // 先批量插 4 条
        logInfoMapper.insertBatch(Arrays.asList(
                build("10.1.0.1"), build("10.1.0.2"),
                build("10.1.0.3"), build("10.1.0.4")));

        Map<String, Object> p = new HashMap<>();
        p.put("start", 0);
        p.put("limit", 2);
        List<LogInfo> page = logInfoMapper.selectByPage(p);
        assertThat(page).hasSize(2);
    }
}
