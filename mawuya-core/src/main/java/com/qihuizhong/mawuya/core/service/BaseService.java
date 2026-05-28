/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.common.utils.PageUtil;
import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.mapper.BaseMapper;
import com.qihuizhong.mawuya.core.vo.Page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service 基类
 *
 * @author zqh
 */
public class BaseService<T, PK extends Serializable> {

    private final BaseMapper<T, PK> baseMapper;

    public BaseService(BaseMapper<T, PK> baseMapper) {
        this.baseMapper = baseMapper;
    }

    public int insert(T t) {
        return baseMapper.insert(t);
    }

    public int deleteById(PK pk) {
        return baseMapper.deleteById(pk);
    }

    public int update(T t) {
        return baseMapper.update(t);
    }

    public T selectById(PK pk) {
        return baseMapper.selectById(pk);
    }

    public List<T> selectList(Map<String, ?> map) {
        return baseMapper.selectList(map);
    }

    /**
     * 分页查询
     *
     * @param curr  第几页（从 1 开始）
     * @param limit 每页条数
     */
    @SuppressWarnings("unchecked")
    public Page<ArticleInfo> selectByPage(Integer curr, Integer limit) {
        if (curr == null || limit == null || curr < 1 || limit < 1) {
            curr = 1;
            limit = 10;
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("start", (curr - 1) * limit);
        map.put("limit", limit);
        List<T> ts = baseMapper.selectByPage(map);

        map.clear();
        int total = baseMapper.selectCount(map);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleInfo> page = new Page<>();
        page.setLists((List<ArticleInfo>) ts)
                .setCurr(curr)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize))
                .setSize(limit)
                .setPageSize(pageSize);
        return page;
    }

    public int getCount(Map<String, ?> map) {
        return baseMapper.selectCount(map);
    }
}
