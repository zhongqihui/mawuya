/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.common.utils.PageUtil;
import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.mapper.BaseMapper;
import com.niudeyapi.mawuya.core.vo.Page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service 基类。
 *
 * <p>遵循《阿里巴巴 Java 开发手册》Service 方法命名规约：
 * <ul>
 *   <li>{@code save} —— 新增（替代 insert）</li>
 *   <li>{@code removeById} —— 删除（替代 deleteById）</li>
 *   <li>{@code updateById} —— 更新（替代 update）</li>
 *   <li>{@code getById} —— 取单个（替代 selectById）</li>
 *   <li>{@code list} —— 取多个（替代 selectList）</li>
 *   <li>{@code listByPage} —— 分页（替代 selectByPage）</li>
 *   <li>{@code count} —— 计数（替代 getCount）</li>
 * </ul>
 *
 * <p>Mapper 层保留 {@code selectXxx / insertXxx / updateXxx / deleteXxx} 命名，对应 SQL 关键字，
 * 阿里规约对 DAO 层无前缀强制约束。</p>
 *
 * @author 钟启辉
 * @param <T>  数据对象类型
 * @param <PK> 主键类型
 */
public class BaseService<T, PK extends Serializable> {

    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 10;
    /** 分页 Map 初始容量 */
    private static final int PAGE_MAP_CAPACITY = 8;

    private final BaseMapper<T, PK> baseMapper;

    public BaseService(BaseMapper<T, PK> baseMapper) {
        this.baseMapper = baseMapper;
    }

    /** 新增（save）。 */
    public int save(T t) {
        return baseMapper.insert(t);
    }

    /** 按主键删除（removeById）。 */
    public int removeById(PK pk) {
        return baseMapper.deleteById(pk);
    }

    /** 按主键更新（updateById）。 */
    public int updateById(T t) {
        return baseMapper.update(t);
    }

    /** 按主键查询单条（getById）。 */
    public T getById(PK pk) {
        return baseMapper.selectById(pk);
    }

    /** 条件查询列表（list）。 */
    public List<T> list(Map<String, ?> map) {
        return baseMapper.selectList(map);
    }

    /**
     * 分页查询（listByPage）。
     *
     * @param curr  第几页（从 1 开始）
     * @param limit 每页条数
     */
    @SuppressWarnings("unchecked")
    public Page<ArticleDO> listByPage(Integer curr, Integer limit) {
        if (curr == null || limit == null || curr < 1 || limit < 1) {
            curr = 1;
            limit = DEFAULT_PAGE_SIZE;
        }

        Map<String, Integer> map = new HashMap<>(PAGE_MAP_CAPACITY);
        map.put("start", (curr - 1) * limit);
        map.put("limit", limit);
        List<T> ts = baseMapper.selectByPage(map);

        map.clear();
        int total = baseMapper.selectCount(map);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleDO> page = new Page<>();
        page.setLists((List<ArticleDO>) ts)
                .setCurr(curr)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize))
                .setSize(limit)
                .setPageSize(pageSize);
        return page;
    }

    /** 条件计数（count）。 */
    public int count(Map<String, ?> map) {
        return baseMapper.selectCount(map);
    }
}
