/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.LogInfo;
import com.qihuizhong.mawuya.core.mapper.LogInfoMapper;
import com.qihuizhong.mawuya.core.vo.LogInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 访客日志业务层。
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>多条件 + 时间范围 + 排序的分页查询</li>
 *   <li>对 controller 层入参做合法性收敛 + 排序字段白名单校验，防止 SQL 注入</li>
 *   <li>时间字符串简单格式校验，避免把垃圾串塞入 req_time 比较</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Service
public class LogInfoService {

    /** 允许的排序列（白名单），与表字段一致；其他列拒绝排序 */
    private static final Set<String> ALLOWED_ORDER_FIELDS = new HashSet<>(Arrays.asList(
            "sn", "ip_addr", "country", "province", "city",
            "req_time", "resp_time", "consume_time", "req_url",
            "req_method", "resp_status"
    ));

    /** 允许的请求方法 */
    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
    ));

    /** 允许的响应状态 */
    private static final Set<String> ALLOWED_RESP_STATUS = new HashSet<>(Arrays.asList("0", "1"));

    /** 时间字符串简单格式校验：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2}(:\\d{2})?)?$"
    );

    private final LogInfoMapper logInfoMapper;

    @Autowired
    public LogInfoService(LogInfoMapper logInfoMapper) {
        this.logInfoMapper = logInfoMapper;
    }

    /** 查询：返回当前页数据 */
    public List<LogInfo> queryPage(LogInfoQuery raw, int page, int size) {
        LogInfoQuery q = sanitize(raw);
        int safePage = page < 1 ? 1 : page;
        int safeSize = (size < 1 || size > 200) ? 20 : size;
        q.setOffset((safePage - 1) * safeSize).setLimit(safeSize);
        try {
            return logInfoMapper.selectByConditionPage(q);
        } catch (Exception e) {
            // 任何 mapper 异常都不应让管理后台 500，给个空列表 + controller 兜个错误码
            return Collections.emptyList();
        }
    }

    /** 计数：与 queryPage 共享同一个 sanitized query */
    public int count(LogInfoQuery raw) {
        try {
            return logInfoMapper.countByCondition(sanitize(raw));
        } catch (Exception e) {
            return 0;
        }
    }

    public LogInfo getById(Integer sn) {
        if (sn == null || sn <= 0) return null;
        return logInfoMapper.selectById(sn);
    }

    /**
     * 对入参做白名单/格式校验：
     *   - 字符串字段 trim、空串 → null
     *   - reqMethod / respStatus 不在白名单 → null（不过滤）
     *   - startTime/endTime 不符合 yyyy-MM-dd[ HH:mm[:ss]] → null
     *   - orderField 不在白名单 → 默认 sn
     *   - orderDir 不在 ASC/DESC → 默认 DESC
     */
    private LogInfoQuery sanitize(LogInfoQuery in) {
        LogInfoQuery q = new LogInfoQuery();
        if (in == null) {
            in = new LogInfoQuery();
        }
        q.setIpAddr(blankToNull(in.getIpAddr()));
        q.setReqUrl(blankToNull(in.getReqUrl()));
        q.setCountry(blankToNull(in.getCountry()));
        q.setProvince(blankToNull(in.getProvince()));

        String method = upperOrNull(in.getReqMethod());
        q.setReqMethod(method != null && ALLOWED_METHODS.contains(method) ? method : null);

        String status = blankToNull(in.getRespStatus());
        q.setRespStatus(status != null && ALLOWED_RESP_STATUS.contains(status) ? status : null);

        q.setStartTime(validTimeOrNull(in.getStartTime()));
        q.setEndTime(validTimeOrNull(in.getEndTime()));

        // 排序白名单
        String orderField = blankToNull(in.getOrderField());
        if (orderField == null || !ALLOWED_ORDER_FIELDS.contains(orderField)) {
            orderField = "sn";
        }
        q.setOrderField(orderField);

        String orderDir = upperOrNull(in.getOrderDir());
        if (!"ASC".equals(orderDir) && !"DESC".equals(orderDir)) {
            orderDir = "DESC";
        }
        q.setOrderDir(orderDir);

        return q;
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String upperOrNull(String s) {
        String t = blankToNull(s);
        return t == null ? null : t.toUpperCase();
    }

    private static String validTimeOrNull(String s) {
        String t = blankToNull(s);
        if (t == null) return null;
        return TIME_PATTERN.matcher(t).matches() ? t : null;
    }
}
