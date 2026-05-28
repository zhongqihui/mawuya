/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import com.qihuizhong.mawuya.core.mapper.CategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客分类业务层
 *
 * @author zqh
 */
@Service
public class CategoryService extends BaseService<Category, Integer> {

    private final ArticleInfoMapper articleInfoMapper;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryMapper baseMapper, ArticleInfoMapper articleInfoMapper) {
        super(baseMapper);
        this.categoryMapper = baseMapper;
        this.articleInfoMapper = articleInfoMapper;
    }

    /**
     * 获取所有分类，并填充每个分类下的文章数
     */
    public List<Category> getCategoryList() {
        Map<String, Object> map = new HashMap<>();
        List<Category> categories = categoryMapper.selectList(map);
        if (categories != null && !categories.isEmpty()) {
            for (Category c : categories) {
                Map<String, Object> condition = new HashMap<>();
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
     * 获取某分类，并填充该分类下的所有文章
     */
    public Category getCategoryBySn(Integer sn) {
        Category category = categoryMapper.selectById(sn);
        if (category != null) {
            Map<String, Object> map = new HashMap<>();
            // 同上：需以 String 形式传入，避免 OGNL 类型转换失败
            map.put("categorySn", String.valueOf(category.getSn()));
            List<ArticleInfo> infos = articleInfoMapper.selectList(map);
            category.setArts(infos);
        }
        return category;
    }

    /**
     * 删除博客分类，同时将该分类下的文章 categorySn 修改为 0（暂无分类）
     */
    public String delCategory(String sn) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return "fail";
        }

        int delCount = categoryMapper.deleteById(id);
        if (delCount <= 0) {
            return "fail";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("category", sn);
        List<ArticleInfo> list = articleInfoMapper.selectAllNoContent(map);
        StringBuilder sb = new StringBuilder();
        for (ArticleInfo a : list) {
            sb.append(a.getSn()).append(",");
        }

        String aSns = sb.toString();
        if (StringUtils.isNotEmpty(aSns) && aSns.endsWith(",")) {
            aSns = aSns.substring(0, aSns.length() - 1);
            articleInfoMapper.updateBatchCategorySn(aSns);
        }

        return "success";
    }
}
