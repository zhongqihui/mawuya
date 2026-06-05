/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller.api;

import com.niudeyapi.mawuya.bms.dto.request.ArticleCoverFromLibraryRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleCreateRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleUpdateRequest;
import com.niudeyapi.mawuya.bms.dto.request.SnRequest;
import com.niudeyapi.mawuya.bms.dto.response.ImageUploadResponse;
import com.niudeyapi.mawuya.core.common.BaseResponse;
import com.niudeyapi.mawuya.core.common.web.SkipApiResponseWrap;
import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.enums.ResultCodeEnum;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.service.ArticleService;
import com.niudeyapi.mawuya.core.service.ImageBlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文章管理 JSON API。
 *
 * <h3>API 列表</h3>
 * <ul>
 *   <li>{@code POST /bms/api/article/create}                创建文章</li>
 *   <li>{@code POST /bms/api/article/update}                更新文章</li>
 *   <li>{@code POST /bms/api/article/delete}                删除文章及其评论</li>
 *   <li>{@code POST /bms/api/article/cover/upload}          上传新封面图（multipart）</li>
 *   <li>{@code POST /bms/api/article/cover/from-library}    从图片库挑选封面</li>
 *   <li>{@code POST /bms/api/article/editor/img-upload}     editor.md 内嵌图片上传（保留旧 JSON 契约）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/article")
public class ArticleApiController {

    private static final Logger log = LoggerFactory.getLogger(ArticleApiController.class);

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_IMG_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));
    /** 单文件最大字节：10 MB */
    private static final long MAX_IMG_BYTES = 10L * 1024 * 1024;

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ImageBlobService imageBlobService;

    @PostMapping("create")
    public BaseResponse<Void> create(@Valid ArticleCreateRequest req) {
        ArticleDO a = new ArticleDO()
                .setArticleTitle(req.getArticleTitle())
                .setArticleSummary(req.getArticleSummary())
                .setArticleContent(req.getArticleContent());
        if (req.getCategorySn() != null) {
            a.setCategorySn(req.getCategorySn());
        }
        if (articleService.save(a) <= 0) {
            throw new BusinessException("文章创建失败");
        }
        return BaseResponse.success("发布成功", null);
    }

    @PostMapping("update")
    public BaseResponse<Void> update(@Valid ArticleUpdateRequest req) {
        ArticleDO dbArticle = articleService.getById(req.getSn());
        if (dbArticle == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "文章不存在");
        }
        dbArticle.setArticleContent(req.getArticleContent())
                .setArticleSummary(req.getArticleSummary())
                .setArticleTitle(req.getArticleTitle());
        if (req.getCategorySn() != null) {
            dbArticle.setCategorySn(req.getCategorySn());
        }
        if (articleService.updateById(dbArticle) <= 0) {
            throw new BusinessException("文章更新失败");
        }
        return BaseResponse.success("更新成功", null);
    }

    @PostMapping("delete")
    public BaseResponse<Void> delete(@Valid SnRequest req) {
        String result = articleService.removeWithReview(String.valueOf(req.getSn()));
        if (!"success".equals(result)) {
            throw new BusinessException("文章删除失败");
        }
        return BaseResponse.success("已删除", null);
    }

    /**
     * 上传新图片作为文章封面：将字节落库 image_blob，再把 picture_url 设为 /image/db/{imgSn}。
     */
    @PostMapping("cover/upload")
    public BaseResponse<ImageUploadResponse> coverUpload(@RequestParam("backgroundImg") MultipartFile file,
                                                         @RequestParam("sn") Integer sn) {
        if (sn == null || sn <= 0) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "sn 不合法");
        }
        validateImage(file);
        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : guessContentType(file.getOriginalFilename());
            long imgSn = imageBlobService.save(bytes, file.getOriginalFilename(), contentType, null);
            ArticleDO info = new ArticleDO().setSn(sn).setPictureUrl("/image/db/" + imgSn);
            articleService.updatePictureUrlById(info);
            return BaseResponse.success("已设置封面",
                    new ImageUploadResponse(imgSn, "/image/db/" + imgSn,
                            file.getOriginalFilename(), (long) bytes.length));
        } catch (IOException e) {
            log.error("[bms/article] cover upload failed", e);
            throw new BusinessException(ResultCodeEnum.UPLOAD_FAILED, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 从图片库挑选已有图片作为封面（不新上传）。
     *
     * <p>安全：picture_url 由服务端按白名单格式 "/image/db/" + imageSn 拼装，
     * imageSn 类型为 Long 由 Spring 强制转换，无 SQL 注入与 URL 注入空间。</p>
     */
    @PostMapping("cover/from-library")
    public BaseResponse<Void> coverFromLibrary(@Valid ArticleCoverFromLibraryRequest req) {
        if (imageBlobService.getMetaById(req.getImageSn()) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "图片不存在");
        }
        ArticleDO info = new ArticleDO()
                .setSn(req.getSn())
                .setPictureUrl("/image/db/" + req.getImageSn());
        if (articleService.updatePictureUrlById(info) <= 0) {
            throw new BusinessException("封面设置失败");
        }
        return BaseResponse.success("已设置封面", null);
    }

    /**
     * editor.md 编辑器内嵌图片上传。
     *
     * <p><strong>本接口必须保持 editor.md 强约定的响应结构</strong>
     * {@code {"success":1, "url":"...", "message":"..."}}，否则编辑器无法识别上传结果，
     * 因此显式标 {@link SkipApiResponseWrap} 跳过 BaseResponse 包装。</p>
     */
    @PostMapping("editor/img-upload")
    @SkipApiResponseWrap
    public Map<String, Object> editorImgUpload(@RequestParam("editormd-image-file") MultipartFile file) {
        Map<String, Object> res = new LinkedHashMap<>(4);
        try {
            validateImage(file);
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : guessContentType(file.getOriginalFilename());
            long imgSn = imageBlobService.save(bytes, file.getOriginalFilename(), contentType, null);
            res.put("success", 1);
            res.put("url", "/image/db/" + imgSn);
            res.put("message", "upload success!");
        } catch (BusinessException ex) {
            res.put("success", 0);
            res.put("url", "");
            res.put("message", ex.getMessage());
        } catch (IOException e) {
            log.error("[bms/article] editor img upload failed", e);
            res.put("success", 0);
            res.put("url", "");
            res.put("message", "upload failed: " + e.getMessage());
        }
        return res;
    }

    // ---------------- helpers ----------------

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_INVALID, "请选择图片");
        }
        if (file.getSize() > MAX_IMG_BYTES) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_TOO_LARGE,
                    "图片大小不能超过 " + (MAX_IMG_BYTES / 1024 / 1024) + "MB");
        }
        String ext = extOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED_IMG_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_INVALID, "仅支持 " + ALLOWED_IMG_EXT + " 格式");
        }
    }

    private String extOf(String fileName) {
        if (fileName == null) return null;
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 && dot < fileName.length() - 1 ? fileName.substring(dot + 1) : null;
    }

    private String guessContentType(String fileName) {
        String ext = extOf(fileName);
        if (ext == null) return "application/octet-stream";
        switch (ext.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
