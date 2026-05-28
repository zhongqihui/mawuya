/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.LogInfo;
import com.qihuizhong.mawuya.core.service.LogInfoService;
import com.qihuizhong.mawuya.core.vo.LogInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BMS 访客日志查询 controller。
 *
 * <p>菜单入口：左侧栏【日志管理 → 访客记录】（id=7）。</p>
 * <p>路由：</p>
 * <ul>
 *   <li>{@code GET /bms/log/list.do}      列表页（带筛选条 + DataTables 容器）</li>
 *   <li>{@code GET /bms/log/query.do}     AJAX 查询（多条件 + 时间范围 + 排序 + 分页，返回 JSON）</li>
 *   <li>{@code GET /bms/log/detail.do}    单条详情（参数/User-Agent/异常等大字段，弹窗展示）</li>
 * </ul>
 *
 * <p>入参均为可选；过滤、排序与分页参数由 {@link LogInfoService#queryPage} 收敛，
 * 排序字段走白名单防 SQL 注入。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/log")
public class LogController extends BaseController {

    @Autowired
    private LogInfoService logInfoService;

    @GetMapping("list.do")
    public String list(Model model) {
        // 列表页本身只渲染骨架；数据由 query.do 异步拉取
        return "bms/log/log_list";
    }

    @GetMapping("query.do")
    @ResponseBody
    public Map<String, Object> query(
            @RequestParam(value = "ipAddr",     required = false) String ipAddr,
            @RequestParam(value = "reqUrl",     required = false) String reqUrl,
            @RequestParam(value = "reqMethod",  required = false) String reqMethod,
            @RequestParam(value = "respStatus", required = false) String respStatus,
            @RequestParam(value = "country",    required = false) String country,
            @RequestParam(value = "province",   required = false) String province,
            @RequestParam(value = "startTime",  required = false) String startTime,
            @RequestParam(value = "endTime",    required = false) String endTime,
            @RequestParam(value = "orderField", required = false) String orderField,
            @RequestParam(value = "orderDir",   required = false) String orderDir,
            @RequestParam(value = "page",       required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size",       required = false, defaultValue = "20") Integer size) {

        LogInfoQuery q = new LogInfoQuery()
                .setIpAddr(ipAddr)
                .setReqUrl(reqUrl)
                .setReqMethod(reqMethod)
                .setRespStatus(respStatus)
                .setCountry(country)
                .setProvince(province)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setOrderField(orderField)
                .setOrderDir(orderDir);

        int total = logInfoService.count(q);
        List<LogInfo> list = logInfoService.queryPage(q, page, size);

        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = (size == null || size < 1 || size > 200) ? 20 : size;
        int pages = (total + safeSize - 1) / safeSize;

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", 1);
        res.put("list", list);
        res.put("total", total);
        res.put("page", safePage);
        res.put("size", safeSize);
        res.put("pages", pages);
        return res;
    }

    @GetMapping("detail.do")
    @ResponseBody
    public Map<String, Object> detail(@RequestParam("sn") Integer sn) {
        Map<String, Object> res = new LinkedHashMap<>();
        LogInfo info = logInfoService.getById(sn);
        if (info == null) {
            res.put("success", 0);
            res.put("message", "记录不存在");
            return res;
        }
        res.put("success", 1);
        res.put("data", info);
        return res;
    }
}
