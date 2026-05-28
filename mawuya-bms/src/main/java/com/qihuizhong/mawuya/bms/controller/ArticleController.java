/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.service.CategoryService;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台文章管理 controller
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms/article")
public class ArticleController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ImageBlobService imageBlobService;

    @GetMapping("toAdd.do")
    public String toWriteArticle(Model model) {
        List<Category> categories = categoryService.selectList(new HashMap<>());
        model.addAttribute("categoryList", categories);
        return "bms/article/write_article";
    }

    @PostMapping("addSubmit.do")
    @ResponseBody
    public String publishArticle(ArticleInfo articleInfo) {
        int count = articleService.insert(articleInfo);
        return count > 0 ? "success" : "fail";
    }

    @GetMapping("list.do")
    public String articleList(Model model) {
        Map<String, String> map = new HashMap<>();
        List<ArticleInfo> articleList = articleService.getAllNoContent(map);
        List<Category> categoryList = categoryService.selectList(map);

        model.addAttribute("articleList", articleList)
                .addAttribute("categoryList", categoryList);
        return "bms/article/article_list";
    }

    @RequestMapping("delSubmit.do")
    @ResponseBody
    public String delSubmit(String sn) {
        return articleService.delArtcleAndReview(sn);
    }

    @RequestMapping("toUpdate.do")
    public String toUpdate(String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        ArticleInfo articleInfo = articleService.selectById(id);
        List<Category> categories = categoryService.selectList(new HashMap<>());
        model.addAttribute("categoryList", categories)
                .addAttribute("article", articleInfo);
        return "bms/article/mod_article";
    }

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

    /** 允许的图片扩展名 */
    private static final java.util.Set<String> ALLOWED_IMG_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));
    /** 单文件最大字节：10 MB */
    private static final long MAX_IMG_BYTES = 10L * 1024 * 1024;

    /**
     * 首页每篇博客的背景图片：上传到 image_blob 表，picture_url 字段写入 /image/db/{sn}
     */
    @RequestMapping("backgroundImgUpload.do")
    @ResponseBody
    public String backgroundImgUpload(@RequestParam(value = "backgroundImg") MultipartFile backgroundImg, String sn) {
        if (StringUtils.isEmpty(sn)) {
            return "fail";
        }
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return "fail";
        }
        if (!isValidImage(backgroundImg)) {
            return "fail";
        }

        try {
            byte[] bytes = backgroundImg.getBytes();
            String contentType = backgroundImg.getContentType() != null
                    ? backgroundImg.getContentType()
                    : guessContentType(backgroundImg.getOriginalFilename());
            long imgSn = imageBlobService.save(bytes, backgroundImg.getOriginalFilename(), contentType, null);
            ArticleInfo articleInfo = new ArticleInfo()
                    .setSn(id)
                    .setPictureUrl("/image/db/" + imgSn);
            articleService.updatePictureUrl(articleInfo);
            return "success";
        } catch (IOException e) {
            log.error("background upload failed", e);
            return "fail";
        }
    }

    /**
     * 从图片库挑选已有图片作为博客背景图（不再新上传）。
     *
     * <p>入参：</p>
     * <ul>
     *   <li>{@code sn}        —— 文章 sn</li>
     *   <li>{@code imageSn}   —— 图片库中已有图片的 sn（image_blob.sn）</li>
     * </ul>
     * <p>处理：校验 sn / imageSn 合法 + 校验 imageSn 真实存在 → 把 picture_url 设为 /image/db/{imageSn}。</p>
     * <p>安全要点：picture_url 由服务端按白名单格式 "/image/db/" + imageSn 拼装，
     * imageSn 类型为 Long 由 Spring 强制转换，无 SQL 注入与 URL 注入空间。</p>
     */
    @PostMapping("setBackgroundFromLibrary.do")
    @ResponseBody
    public String setBackgroundFromLibrary(@RequestParam(value = "sn") String sn,
                                           @RequestParam(value = "imageSn") Long imageSn) {
        if (StringUtils.isEmpty(sn) || imageSn == null || imageSn <= 0) {
            return "fail";
        }
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return "fail";
        }
        // 校验图片真实存在于库中（loadMeta 不取二进制，开销极小）
        if (imageBlobService.loadMeta(imageSn) == null) {
            return "fail";
        }
        ArticleInfo articleInfo = new ArticleInfo()
                .setSn(id)
                .setPictureUrl("/image/db/" + imageSn);
        return articleService.updatePictureUrl(articleInfo) > 0 ? "success" : "fail";
    }

    /**
     * 编辑器中插入图片：同样落库，返回 /image/db/{sn} 给 editor.md
     */
    @PostMapping("imgUpload.do")
    @ResponseBody
    public Map<String, Object> imgUpload(@RequestParam(value = "editormd-image-file") MultipartFile file) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (!isValidImage(file)) {
            res.put("url", "");
            res.put("success", 0);
            res.put("message", "仅支持 jpg/png/gif/webp 等图片格式，且大小不超过 10MB");
            return res;
        }
        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : guessContentType(file.getOriginalFilename());
            long imgSn = imageBlobService.save(bytes, file.getOriginalFilename(), contentType, null);
            res.put("url", "/image/db/" + imgSn);
            res.put("success", 1);
            res.put("message", "upload success!");
        } catch (IOException e) {
            log.error("imgUpload failed", e);
            res.put("url", "");
            res.put("success", 0);
            res.put("message", "upload failed: " + e.getMessage());
        }
        return res;
    }

    private boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        if (file.getSize() > MAX_IMG_BYTES) {
            return false;
        }
        String name = file.getOriginalFilename();
        if (name == null) return false;
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return false;
        return ALLOWED_IMG_EXT.contains(name.substring(dot + 1).toLowerCase());
    }

    private String guessContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "application/octet-stream";
        switch (fileName.substring(dot + 1).toLowerCase()) {
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png":  return "image/png";
            case "gif":  return "image/gif";
            case "bmp":  return "image/bmp";
            case "webp": return "image/webp";
            default:     return "application/octet-stream";
        }
    }
}
