/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller.api;

import com.qihuizhong.mawuya.bms.dto.request.CategoryCreateRequest;
import com.qihuizhong.mawuya.bms.dto.request.CategoryUpdateRequest;
import com.qihuizhong.mawuya.bms.dto.request.SnRequest;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 分类管理 JSON API。
 *
 * <ul>
 *   <li>{@code POST /bms/api/category/create}  创建</li>
 *   <li>{@code POST /bms/api/category/update}  更新</li>
 *   <li>{@code POST /bms/api/category/delete}  删除（同步将该分类下文章 categorySn 置 0）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/category")
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("create")
    public BaseResponse<Void> create(@Valid CategoryCreateRequest req) {
        Category c = new Category().setCategoryName(req.getCategoryName().trim());
        if (categoryService.insert(c) <= 0) {
            throw new BusinessException("创建失败");
        }
        return BaseResponse.success("创建成功", null);
    }

    @PostMapping("update")
    public BaseResponse<Void> update(@Valid CategoryUpdateRequest req) {
        Category c = new Category()
                .setSn(req.getSn())
                .setCategoryName(req.getCategoryName().trim());
        if (categoryService.update(c) <= 0) {
            throw new BusinessException("更新失败：分类不存在");
        }
        return BaseResponse.success("更新成功", null);
    }

    @PostMapping("delete")
    public BaseResponse<Void> delete(@Valid SnRequest req) {
        String result = categoryService.delCategory(String.valueOf(req.getSn()));
        if (!"success".equals(result)) {
            throw new BusinessException("删除失败");
        }
        return BaseResponse.success("已删除", null);
    }
}
