package com.zqh.blog.controller.bms;

import com.zqh.blog.controller.BaseController;
import com.zqh.blog.entity.Category;
import com.zqh.blog.service.CategoryService;
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
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/8 11:19
 * description: 博客分类controller
 **/
@Controller
@RequestMapping("bms/category")
public class CategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("categoryList.do")
    public String categoryList(Model model) {
        long l = System.currentTimeMillis();
        List<Category> categoryList = categoryService.selectList(new HashMap());
        model.addAttribute("categoryList", categoryList);
        System.out.println(System.currentTimeMillis() -l);
        return "bms/category/category_list";
    }

    @GetMapping("toAdd.do")
    public String toAdd() {
        return "bms/category/add_category";
    }

    @PostMapping("addSubmit.do")
    @ResponseBody
    public String addSubmit(Category category) {
        categoryService.insert(category);
        return "success";
    }

    @RequestMapping("toUpdate.do")
    public String toUpdate(Category category, Model model) {
        model.addAttribute("category", category);
        return "bms/category/update_category";
    }

    @PostMapping("updateSubmit.do")
    @ResponseBody
    public String updateSubmit(Category category) {
        categoryService.update(category);
        return "success";
    }

    @RequestMapping("delSubmit.do")
    @ResponseBody
    public String delSubmit(String sn) {
        categoryService.deleteById(Integer.parseInt(sn));
        return "success";
    }

}
