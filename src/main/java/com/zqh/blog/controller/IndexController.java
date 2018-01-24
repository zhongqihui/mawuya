package com.zqh.blog.controller;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.service.ArticleService;
import com.zqh.blog.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/21 0021 下午 9:13
 * description: 博客前台controller，用于展示
 **/
@Controller
public class IndexController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    ArticleService articleService;

    @RequestMapping({"index.html", "index", "index.jsp"})
    public String toHomePage(Model model, HttpServletRequest request) {
        Page<ArticleInfo> page = articleService.getPage(request);
        page.setUrl("index");
        model.addAttribute("page", page);
        return "fts/index";
    }

    /**
     * 根据文章id，展示该文章
     *
     * @param aid
     * @param model
     * @return
     */
    @RequestMapping("{aid}")
    public String getArticle(@PathVariable("aid") String aid, Model model) {
        Integer sn = null;
        try {
            sn = Integer.parseInt(aid);
        } catch (Exception e) {
            return ret404Page();
        }

        ArticleInfo info = articleService.selectById(sn);
        if (info == null) {
            return ret404Page();
        }

        ArticleInfo next = articleService.getNext(sn);
        ArticleInfo prev = articleService.getPrev(sn);
        model.addAttribute("article", info)
                .addAttribute("next", next)
                .addAttribute("prev", prev);
        return "fts/showArticle";
    }
}
