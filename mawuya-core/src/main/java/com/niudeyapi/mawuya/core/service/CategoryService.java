/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.dataobject.CategoryDO;
import com.niudeyapi.mawuya.core.mapper.ArticleInfoMapper;
import com.niudeyapi.mawuya.core.mapper.CategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客分类业务层。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code get/list/count/save/update/remove}。</p>
 *
 * @author 钟启辉
 */
@Service
public class CategoryService extends BaseService<CategoryDO, Integer> {

    /** Map 初始容量：mapper.xml 条件入参常量级 key（≤4） */
    private static final int CONDITION_MAP_CAPACITY = 4;

    /** 业务返回常量：成功 */
    public static final String OPS_SUCCESS = "success";
    /** 业务返回常量：失败 */
    public static final String OPS_FAIL    = "fail";

    private final ArticleInfoMapper articleInfoMapper;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryMapper baseMapper, ArticleInfoMapper articleInfoMapper) {
        super(baseMapper);
        this.categoryMapper = baseMapper;
        this.articleInfoMapper = articleInfoMapper;
    }

    /**
     * 列出所有分类，并填充每个分类下的文章数（{@code artSize}）。
     */
    public List<CategoryDO> listAllWithArtSize() {
        Map<String, Object> map = new HashMap<>(CONDITION_MAP_CAPACITY);
        List<CategoryDO> categories = categoryMapper.selectList(map);
        if (categories != null && !categories.isEmpty()) {
            for (CategoryDO c : categories) {
                Map<String, Object> condition = new HashMap<>(CONDITION_MAP_CAPACITY);
                // mapper.xml 使用 OGNL @StringUtils@isNotEmpty(categorySn) 判空，
                // 该方法只接受 CharSequence；此处必须用 String 传入。
                condition.put("categorySn", String.valueOf(c.getSn()));
                int i = articleInfoMapper.selectCount(condition);
                c.setArtSize(i);
            }
        }
        return categories;
    }

    /**
     * 取分类详情，并填充该分类下的所有文章。
     */
    public CategoryDO getDetailWithArtsBySn(Integer sn) {
        CategoryDO category = categoryMapper.selectById(sn);
        if (category != null) {
            Map<String, Object> map = new HashMap<>(CONDITION_MAP_CAPACITY);
            // 同上：需以 String 形式传入，避免 OGNL 类型转换失败
            map.put("categorySn", String.valueOf(category.getSn()));
            List<ArticleDO> infos = articleInfoMapper.selectList(map);
            category.setArts(infos);
        }
        return category;
    }

    /**
     * 删除博客分类，同时将该分类下的文章 categorySn 置 0（暂无分类）。
     */
    public String removeAndOrphanArticles(String sn) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return OPS_FAIL;
        }

        int delCount = categoryMapper.deleteById(id);
        if (delCount <= 0) {
            return OPS_FAIL;
        }

        Map<String, Object> map = new HashMap<>(CONDITION_MAP_CAPACITY);
        map.put("category", sn);
        List<ArticleDO> list = articleInfoMapper.selectAllNoContent(map);
        StringBuilder sb = new StringBuilder();
        for (ArticleDO a : list) {
            sb.append(a.getSn()).append(",");
        }

        String aSns = sb.toString();
        if (StringUtils.isNotEmpty(aSns) && aSns.endsWith(",")) {
            aSns = aSns.substring(0, aSns.length() - 1);
            articleInfoMapper.updateBatchCategorySn(aSns);
        }

        return OPS_SUCCESS;
    }
}
