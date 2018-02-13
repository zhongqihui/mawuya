package com.zqh.blog.controller.bms;

import com.zqh.blog.controller.BaseController;
import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.entity.Category;
import com.zqh.blog.service.ArticleService;
import com.zqh.blog.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/21 0021 下午 9:13
 * description: 博客文章管理后台controller
 **/
@Controller
@RequestMapping("bms/article")
public class ArticleController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    ArticleService articleService;
    @Autowired
    CategoryService categoryService;

    /**
     * 进入编写博客界面
     *
     * @param model
     * @return
     */
    @GetMapping("toAdd.do")
    public String toWriteArticle(Model model) {
        List<Category> categories = categoryService.selectList(new HashMap());
        model.addAttribute("categoryList", categories);
        return "bms/article/write_article";
    }

    /**
     * 发布博客
     *
     * @param articleInfo
     * @return
     */
    @PostMapping("addSubmit.do")
    @ResponseBody
    public String publishArticle(ArticleInfo articleInfo) {
        int count = articleService.insert(articleInfo);
        return count > 0 ? "success" : "fail";
    }

    /**
     * 博客文章列表
     *
     * @param model
     * @return
     */
    @GetMapping("list.do")
    public String articleList(Model model) {
        Map<String, String> map = new HashMap<>();
        List<ArticleInfo> articleList = articleService.getAllNoContent(map);
        List<Category> categoryList = categoryService.selectList(map);

        model.addAttribute("articleList", articleList)
                .addAttribute("categoryList", categoryList);
        return "bms/article/article_list";
    }

    /**
     * 删除博客
     *
     * @param sn
     * @return
     */
    @RequestMapping("delSubmit.do")
    @ResponseBody
    public String delSubmit(String sn) {
        return articleService.delArtcleAndReview(sn);
    }

    /**
     * 进入博客文章修改页面
     *
     * @param sn
     * @param model
     * @return
     */
    @RequestMapping("toUpdate.do")
    public String toUpdate(String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (Exception e) {
            return ret404Page();
        }

        ArticleInfo articleInfo = articleService.selectById(id);
        List<Category> categories = categoryService.selectList(new HashMap());
        model.addAttribute("categoryList", categories)
            .addAttribute("article", articleInfo);
        return "bms/article/write_article";
    }


    /**
     * 修改文章提交
     * @param a
     * @return
     */
    @PostMapping("updateSubmit.do")
    @ResponseBody
    public String updateSubmit(ArticleInfo a) {
        return articleService.update(a) <= 0 ? "fail" : "success";
    }



}
