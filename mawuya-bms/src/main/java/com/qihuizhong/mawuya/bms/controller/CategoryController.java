/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * 后台分类管理 controller
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
        List<Category> categoryList = categoryService.selectList(new HashMap<>());
        model.addAttribute("categoryList", categoryList);
        return "bms/category/category_list";
    }

    @GetMapping("toAdd.do")
    public String toAdd() {
        return "bms/category/mod_category";
    }

    @PostMapping("addSubmit.do")
    @ResponseBody
    public String addSubmit(Category category) {
        return categoryService.insert(category) <= 0 ? "fail" : "success";
    }

    @RequestMapping("toUpdate.do")
    public String toUpdate(String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        Category category = categoryService.selectById(id);
        model.addAttribute("category", category);
        return "bms/category/mod_category";
    }

    @PostMapping("updateSubmit.do")
    @ResponseBody
    public String updateSubmit(Category category) {
        return categoryService.update(category) <= 0 ? "fail" : "success";
    }

    @RequestMapping("delSubmit.do")
    @ResponseBody
    public String delSubmit(String sn) {
        return categoryService.delCategory(sn);
    }
}
