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
     * 图片选择器弹窗页（独立页面，被 layer iframe 加载）。
     * 与图片库主页解耦：仅做"挑选"，不含上传/删除等管理操作。
     */
    @GetMapping("picker.do")
    public String picker() {
        return "bms/image/picker";
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

    /**
     * 列出图片 metadata，支持关键词搜索 + 分页。
     *
     * <p>参数（全部可选，向后兼容）：</p>
     * <ul>
     *   <li>{@code keyword}：按 file_name 模糊匹配，空字符串/缺省视为不过滤</li>
     *   <li>{@code page}：页码（从 1 起），缺省为 1；非法（≤0）按 1 处理</li>
     *   <li>{@code size}：每页条数，缺省 24，强制收敛到 [1, 200]</li>
     * </ul>
     * <p>返回字段：success / list / total / page / size / pages（总页数）</p>
     */
    @GetMapping("list.do")
    @ResponseBody
    public Map<String, Object> list(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "page", required = false) Integer page,
                                    @RequestParam(value = "size", required = false) Integer size) {
        // 入参收敛（防止恶意构造超大 size 把整库拖到内存）
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null) ? 24 : size;
        if (safeSize < 1) safeSize = 1;
        if (safeSize > 200) safeSize = 200;
        int offset = (safePage - 1) * safeSize;

        int total = imageBlobService.countByKeyword(keyword);
        List<ImageBlob> metas = imageBlobService.listMetaByKeyword(keyword, safeSize, offset);

        List<Map<String, Object>> list = metas.stream().map(b -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sn", b.getSn());
            item.put("name", b.getFileName());
            item.put("url", "/image/db/" + b.getSn());
            item.put("size", b.getByteSize());
            item.put("mtime", b.getCreatedTime());
            return item;
        }).collect(Collectors.toList());

        int pages = (total + safeSize - 1) / safeSize;

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", 1);
        res.put("list", list);
        res.put("total", total);
        res.put("page", safePage);
        res.put("size", safeSize);
        res.put("pages", pages);
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
