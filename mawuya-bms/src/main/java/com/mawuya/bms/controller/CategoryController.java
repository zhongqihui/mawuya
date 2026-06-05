/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.controller;

import com.mawuya.core.dataobject.CategoryDO;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

/**
 * 后台分类管理视图 controller。
 *
 * <p>JSON 操作（增删改）已迁移至
 * {@link com.mawuya.bms.controller.api.CategoryApiController}。</p>
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms/category")
public class CategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("categoryList.do")
    public String categoryList(Model model) {
        List<CategoryDO> categoryList = categoryService.list(new HashMap<>(4));
        model.addAttribute("categoryList", categoryList);
        return "bms/category/category_list";
    }

    @GetMapping("toAdd.do")
    public String toAdd() {
        return "bms/category/mod_category";
    }

    @GetMapping("toUpdate.do")
    public String toUpdate(@RequestParam("sn") String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        CategoryDO category = categoryService.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        model.addAttribute("category", category);
        return "bms/category/mod_category";
    }
}
