/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller.api;

import com.niudeyapi.mawuya.bms.dto.request.ArticleCoverFromLibraryRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleCreateRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleDraftRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleUpdateRequest;
import com.niudeyapi.mawuya.bms.dto.request.ArticleWithdrawRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 *   <li>{@code POST /bms/api/article/create}                创建文章（立即发布）</li>
 *   <li>{@code POST /bms/api/article/saveDraft}             保存为草稿（新建或更新）</li>
 *   <li>{@code POST /bms/api/article/publish}               草稿 / 已撤回 → 发布</li>
 *   <li>{@code POST /bms/api/article/withdraw}              已发布 → 已撤回（对外不可见）</li>
 *   <li>{@code POST /bms/api/article/republish}             已撤回 → 已发布（重新发布）</li>
 *   <li>{@code POST /bms/api/article/update}                更新文章（保持当前状态）</li>
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

    /**
     * 创建并立即发布文章。
     *
     * <p>与历史契约保持一致：标题 / 正文必填。需要保存草稿请走 {@link #saveDraft}。
     * 响应 {@code data} 含新文章 {@code sn}，供前端紧接着发起「标签绑定」等后续动作。</p>
     */
    @PostMapping("create")
    public BaseResponse<Map<String, Object>> create(@Valid ArticleCreateRequest req) {
        ArticleDO a = new ArticleDO()
                .setArticleTitle(req.getArticleTitle())
                .setArticleSummary(req.getArticleSummary())
                .setArticleContent(req.getArticleContent());
        if (req.getCategorySn() != null) {
            a.setCategorySn(req.getCategorySn());
        }
        if (articleService.savePublished(a, currentOperator()) <= 0) {
            throw new BusinessException("文章创建失败");
        }
        Map<String, Object> data = new LinkedHashMap<>(2);
        data.put("sn", a.getSn());
        data.put("status", ArticleService.STATUS_PUBLISHED);
        return BaseResponse.success("发布成功", data);
    }

    /**
     * 保存为草稿。
     * <ul>
     *   <li>{@code sn} 为空 → 新建一篇 status=DRAFT 的文章；</li>
     *   <li>{@code sn} 非空 → 更新已有草稿（只允许在 DRAFT 状态下「保存为草稿」）。</li>
     * </ul>
     * 已发布 / 已撤回的文章不能"退回"草稿——请走 {@link #withdraw} 或 {@link #update}。
     */
    @PostMapping("saveDraft")
    public BaseResponse<Map<String, Object>> saveDraft(@Valid ArticleDraftRequest req) {
        String operator = currentOperator();
        ArticleDO a = new ArticleDO()
                .setArticleTitle(req.getArticleTitle())
                .setArticleSummary(req.getArticleSummary())
                .setArticleContent(req.getArticleContent() == null ? "" : req.getArticleContent());
        if (req.getCategorySn() != null) {
            a.setCategorySn(req.getCategorySn());
        }

        Map<String, Object> data = new LinkedHashMap<>(2);
        if (req.getSn() == null) {
            // 新建草稿
            if (articleService.saveDraft(a, operator) <= 0) {
                throw new BusinessException("草稿保存失败");
            }
            data.put("sn", a.getSn());
            data.put("status", ArticleService.STATUS_DRAFT);
            return BaseResponse.success("草稿已保存", data);
        }

        // 更新已有草稿
        a.setSn(req.getSn());
        if (articleService.updateAsDraft(a, operator) <= 0) {
            throw new BusinessException("草稿保存失败");
        }
        data.put("sn", req.getSn());
        data.put("status", ArticleService.STATUS_DRAFT);
        return BaseResponse.success("草稿已保存", data);
    }

    /**
     * 草稿 / 已撤回 → 已发布（状态机迁移）。
     *
     * <p>常见用法：在编辑页打开一篇草稿，先 {@link #update} 提交最新内容，再调本接口"发布"。
     * 也可单纯传 sn 即把草稿/撤回稿原样发布，不修改内容。</p>
     */
    @PostMapping("publish")
    public BaseResponse<Void> publish(@Valid SnRequest req) {
        if (articleService.publish(req.getSn(), currentOperator()) <= 0) {
            throw new BusinessException("发布失败");
        }
        return BaseResponse.success("已发布", null);
    }

    /**
     * 已发布 → 已撤回。
     * <p>撤回后博客对外不可见（AMS 端 404），编辑后可重新发布。</p>
     */
    @PostMapping("withdraw")
    public BaseResponse<Void> withdraw(@Valid ArticleWithdrawRequest req) {
        if (articleService.withdraw(req.getSn(), currentOperator(), req.getRemark()) <= 0) {
            throw new BusinessException("撤回失败");
        }
        return BaseResponse.success("已撤回，可编辑后重新发布", null);
    }

    /** 已撤回 → 已发布。 */
    @PostMapping("republish")
    public BaseResponse<Void> republish(@Valid SnRequest req) {
        if (articleService.republish(req.getSn(), currentOperator()) <= 0) {
            throw new BusinessException("重新发布失败");
        }
        return BaseResponse.success("已重新发布", null);
    }

    /**
     * 更新文章：保持当前状态（已发布仍是已发布、已撤回仍是已撤回、草稿仍是草稿）。
     *
     * <p>不会改变 status。如需流转，请显式调 {@link #publish} / {@link #withdraw} / {@link #republish}。</p>
     */
    @PostMapping("update")
    public BaseResponse<Void> update(@Valid ArticleUpdateRequest req) {
        ArticleDO patch = new ArticleDO()
                .setSn(req.getSn())
                .setArticleContent(req.getArticleContent())
                .setArticleSummary(req.getArticleSummary())
                .setArticleTitle(req.getArticleTitle());
        if (req.getCategorySn() != null) {
            patch.setCategorySn(req.getCategorySn());
        }
        if (articleService.updateKeepStatus(patch, currentOperator()) <= 0) {
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

    /**
     * 取当前 BMS 登录用户的 username，写入状态流转日志的 operator 字段。
     *
     * <p>未登录场景（极少数定时任务 / 单测）返回 null，DB 列允许 NULL。</p>
     */
    private String currentOperator() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            String name = auth.getName();
            return "anonymousUser".equals(name) ? null : name;
        } catch (Exception e) {
            return null;
        }
    }

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
