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

    /**
     * 进入博客分类列表页面
     * @param model
     * @return
     */
    @GetMapping("categoryList.do")
    public String categoryList(Model model) {
        long l = System.currentTimeMillis();
        List<Category> categoryList = categoryService.selectList(new HashMap());
        model.addAttribute("categoryList", categoryList);
        System.out.println(System.currentTimeMillis() -l);
        return "bms/category/category_list";
    }

    /**
     * 进入添加博客分类页面
     * @return
     */
    @GetMapping("toAdd.do")
    public String toAdd() {
        return "bms/category/mod_category";
    }

    /**
     * 添加博客分类
     * @param category
     * @return
     */
    @PostMapping("addSubmit.do")
    @ResponseBody
    public String addSubmit(Category category) {
        return categoryService.insert(category) <= 0? "fail" :"success";
    }

    /**
     * 进入博客分类修改页面
     * @param sn
     * @param model
     * @return
     */
    @RequestMapping("toUpdate.do")
    public String toUpdate(String sn, Model model) {
        int id;
        try{
            id = Integer.parseInt(sn);
        }catch (Exception e) {
            return ret404Page();
        }

        Category category = categoryService.selectById(id);
        model.addAttribute("category", category);
        return "bms/category/mod_category";
    }

    /**
     * 博客分类修改提交
     * @param category
     * @return
     */
    @PostMapping("updateSubmit.do")
    @ResponseBody
    public String updateSubmit(Category category) {
        return categoryService.update(category) <= 0 ? "fail" : "success";
    }

    /**
     * 删除博客分类
     * @param sn
     * @return
     */
    @RequestMapping("delSubmit.do")
    @ResponseBody
    public String delSubmit(String sn) {
        return categoryService.delCategory(sn);
    }

}
