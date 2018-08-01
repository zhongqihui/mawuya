package com.zqh.blog.controller.bms;

import com.zqh.blog.controller.BaseController;
import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.entity.Category;
import com.zqh.blog.service.ArticleService;
import com.zqh.blog.service.CategoryService;
import com.zqh.blog.utils.FileUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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
        return "bms/article/mod_article";
    }


    /**
     * 修改文章提交
     *
     * @param a
     * @return
     */
    @PostMapping("updateSubmit.do")
    @ResponseBody
    public String updateSubmit(ArticleInfo a) {

        ArticleInfo dbArticle = articleService.selectById(a.getSn());
        if (dbArticle == null) {
            return "fail";
        }

        dbArticle.setArticleContent(a.getArticleContent())
                .setArticleSummary(a.getArticleSummary())
                .setArticleTitle(a.getArticleTitle())
                .setCategorySn(a.getCategorySn());
        return articleService.update(dbArticle) <= 0 ? "fail" : "success";
    }


    /**
     * 首页每篇博客的背景图片
     *
     * @param backgroundImg
     * @param sn
     * @return
     */
    @RequestMapping("backgroundImgUpload.do")
    @ResponseBody
    public String backgroundImgUpload(HttpServletRequest request,
                                      @RequestParam(value = "backgroundImg") MultipartFile backgroundImg,
                                      String sn) throws Exception {
        if(StringUtils.isEmpty(sn)) {
            return "fail";
        }

        int id;
        try {
            id = Integer.parseInt(sn);
        }catch (Exception e) {
            return "fail";
        }

        String path = request.getSession().getServletContext().getRealPath("/statics/images/upload/" + sn + "/");
        boolean delete = new File(path).delete();//删除原来的
        String fileName = FileUtil.upload(backgroundImg, path);
        ArticleInfo articleInfo = new ArticleInfo()
                .setSn(id)
                .setPictureUrl(request.getContextPath()+ "/statics/images/upload/" + fileName);
        articleService.updatePictureUrl(articleInfo);
        return "success";
    }


    /**
     * 博客中的图片上传
     *
     * @param file
     * @param request
     * @return
     */
    @PostMapping("imgUpload.do")
    @ResponseBody
    public JSONObject imgUpload(@RequestParam(value = "editormd-image-file") MultipartFile file,
                                HttpServletRequest request) throws Exception {
        String path = request.getSession().getServletContext().getRealPath("/statics/images/upload/");
        String fileName = FileUtil.upload(file, path);
        JSONObject res = new JSONObject();
        res.put("url", request.getContextPath()+ "/statics/images/upload/" + fileName);
        res.put("success", 1);
        res.put("message", "upload success!");
        return res;
    }


}
