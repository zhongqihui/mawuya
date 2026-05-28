/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.ImageBlob;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台图片管理：上传到数据库（image_blob 表），统一通过 /image/db/{sn} 访问。
 *
 * <p>从 v3 起，图片不再落到本地文件系统：
 * <ul>
 *   <li>上传：将字节读入内存 → 计算 sha256 → 写入 image_blob 表（自动去重）</li>
 *   <li>读取：所有 url 形如 {@code /image/db/123}，由 core 模块的 {@link com.qihuizhong.mawuya.core.controller.DbImageController} 输出</li>
 * </ul>
 * 这样 ams（前台）与 bms（后台）共享同一份图片源，不再依赖文件目录。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/image")
public class ImageController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));
    private static final long MAX_BYTES = 10L * 1024 * 1024;

    @Autowired
    private ImageBlobService imageBlobService;

    @GetMapping("library.do")
    public String library() {
        return "bms/image/library";
    }

    /**
     * 通用上传：将图片字节落库到 image_blob 表，返回访问 URL。
     * <p>返回 JSON：{success, url, name, size, message}</p>
     */
    @PostMapping("upload.do")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            validateImage(file);
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : guessContentType(file.getOriginalFilename());
            long sn = imageBlobService.save(
                    bytes,
                    file.getOriginalFilename(),
                    contentType,
                    null);
            res.put("success", 1);
            res.put("url", "/image/db/" + sn);
            res.put("sn", sn);
            res.put("name", file.getOriginalFilename());
            res.put("size", bytes.length);
            res.put("message", "上传成功");
        } catch (IllegalArgumentException ex) {
            res.put("success", 0);
            res.put("url", "");
            res.put("message", ex.getMessage());
        } catch (IOException e) {
            log.error("image upload failed", e);
            res.put("success", 0);
            res.put("url", "");
            res.put("message", "上传失败：" + e.getMessage());
        }
        return res;
    }

    /** 列出最近 200 张图片（DB 中 image_blob 的 metadata） */
    @GetMapping("list.do")
    @ResponseBody
    public Map<String, Object> list() {
        Map<String, Object> res = new LinkedHashMap<>();
        List<ImageBlob> metas = imageBlobService.listMeta(200, 0);
        List<Map<String, Object>> list = metas.stream().map(b -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sn", b.getSn());
            item.put("name", b.getFileName());
            item.put("url", "/image/db/" + b.getSn());
            item.put("size", b.getByteSize());
            item.put("mtime", b.getCreatedTime());
            return item;
        }).collect(Collectors.toList());
        res.put("success", 1);
        res.put("list", list);
        res.put("total", imageBlobService.countAll());
        return res;
    }

    /** 删除图片（按 sn）。注意：仍被 picture_url 引用的不强制清理，业务层自行决定。 */
    @PostMapping("delete.do")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("sn") Long sn) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (sn == null || sn <= 0) {
            res.put("success", 0);
            res.put("message", "非法 sn");
            return res;
        }
        boolean ok = imageBlobService.deleteById(sn);
        res.put("success", ok ? 1 : 0);
        res.put("message", ok ? "已删除" : "图片不存在或删除失败");
        return res;
    }

    // ---------------------- helpers ----------------------

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("图片大小不能超过 " + (MAX_BYTES / 1024 / 1024) + "MB");
        }
        String ext = extOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("仅支持 " + ALLOWED_EXT + " 格式");
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
