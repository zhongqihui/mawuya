/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD Mapper 基类
 *
 * @param <T>  实体类型
 * @param <PK> 主键类型
 */
public interface BaseMapper<T, PK extends Serializable> {

    int insert(T t);

    int deleteById(PK id);

    int update(T t);

    T selectById(PK pk);

    List<T> selectList(Map<String, ?> map);

    /**
     * 分页查询：map 需包含 start、limit 以及可选业务条件
     */
    List<T> selectByPage(Map<String, ?> map);

    int selectCount(Map<String, ?> map);
}
