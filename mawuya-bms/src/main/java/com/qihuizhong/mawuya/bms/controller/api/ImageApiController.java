/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller.api;

import com.qihuizhong.mawuya.bms.dto.request.ImageQueryRequest;
import com.qihuizhong.mawuya.bms.dto.request.SnRequest;
import com.qihuizhong.mawuya.bms.dto.response.ImageMetaResponse;
import com.qihuizhong.mawuya.bms.dto.response.ImageUploadResponse;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import com.qihuizhong.mawuya.core.common.PageResponse;
import com.qihuizhong.mawuya.core.dataobject.ImageDO;
import com.qihuizhong.mawuya.core.enums.ResultCodeEnum;
import com.qihuizhong.mawuya.core.exception.BusinessException;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图片库 JSON API。
 *
 * <ul>
 *   <li>{@code POST /bms/api/image/upload}  通用图片上传（落库 image_blob，返回访问 URL）</li>
 *   <li>{@code GET  /bms/api/image/page}    分页查询元数据（含关键字模糊匹配）</li>
 *   <li>{@code POST /bms/api/image/delete}  按 sn 删除</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/image")
public class ImageApiController {

    private static final Logger log = LoggerFactory.getLogger(ImageApiController.class);

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));
    private static final long MAX_BYTES = 10L * 1024 * 1024;
    /** 单页上限，防止恶意构造大 size 把整库拖到内存 */
    private static final int MAX_PAGE_SIZE = 200;

    @Autowired
    private ImageBlobService imageBlobService;

    @PostMapping("upload")
    public BaseResponse<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        validateImage(file);
        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : guessContentType(file.getOriginalFilename());
            long sn = imageBlobService.save(
                    bytes, file.getOriginalFilename(), contentType, null);
            return BaseResponse.success("上传成功",
                    new ImageUploadResponse(sn, "/image/db/" + sn,
                            file.getOriginalFilename(), (long) bytes.length));
        } catch (IOException e) {
            log.error("[bms/image] upload failed", e);
            throw new BusinessException(ResultCodeEnum.UPLOAD_FAILED, "上传失败：" + e.getMessage());
        }
    }

    @GetMapping("page")
    public BaseResponse<PageResponse<ImageMetaResponse>> page(@Valid ImageQueryRequest req) {
        int safeSize = Math.min(req.getSize(), MAX_PAGE_SIZE);
        int offset = (req.getPage() - 1) * safeSize;

        int total = imageBlobService.countByKeyword(req.getKeyword());
        List<ImageDO> metas = imageBlobService.listMetaByKeyword(req.getKeyword(), safeSize, offset);
        List<ImageMetaResponse> list = metas.stream()
                .map(b -> new ImageMetaResponse(
                        b.getSn(),
                        b.getFileName(),
                        "/image/db/" + b.getSn(),
                        b.getByteSize(),
                        b.getCreatedTime()))
                .collect(Collectors.toList());

        return BaseResponse.success(PageResponse.of(list, total, req.getPage(), safeSize));
    }

    @PostMapping("delete")
    public BaseResponse<Void> delete(@Valid SnRequest req) {
        if (req.getSn() <= 0) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "非法 sn");
        }
        if (!imageBlobService.removeById(req.getSn().longValue())) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "图片不存在或删除失败");
        }
        return BaseResponse.success("已删除", null);
    }

    // ---------------- helpers ----------------

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_INVALID, "请选择图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_TOO_LARGE,
                    "图片大小不能超过 " + (MAX_BYTES / 1024 / 1024) + "MB");
        }
        String ext = extOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_INVALID, "仅支持 " + ALLOWED_EXT + " 格式");
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
