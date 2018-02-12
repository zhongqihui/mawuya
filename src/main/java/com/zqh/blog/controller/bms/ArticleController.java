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
import org.springframework.web.bind.annotation.*;

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
        return "bms/article/writeArticle";
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


}
