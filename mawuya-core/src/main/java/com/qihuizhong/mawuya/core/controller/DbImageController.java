/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.controller;

import com.qihuizhong.mawuya.core.entity.ImageBlob;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 图片二进制读取（按 sn 流式输出）。
 *
 * <p>路径形如 {@code /image/db/123} 或 {@code /image/db/123.png}（带扩展名兼容老 .md 引用）。</p>
 *
 * <p>该控制器位于 core 模块，ams / bms 都通过 {@code @ComponentScan} 加载它，
 * 所以两边访问 {@code /image/db/{sn}} 都能拿到同一份数据。</p>
 *
 * @author 钟启辉
 */
@org.springframework.stereotype.Controller
@RequestMapping("image/db")
public class DbImageController {

    /** 浏览器缓存 30 天（图片内容不可变，因为 sha256 唯一） */
    private static final long CACHE_SECONDS = 30L * 24 * 3600;

    @Autowired
    private ImageBlobService imageBlobService;

    /** 形如 /image/db/123、/image/db/123.png、/image/db/123.jpg 都可命中 */
    @GetMapping({"/{sn}", "/{sn}.{ext}"})
    public ResponseEntity<byte[]> get(@PathVariable("sn") String snStr,
                                      @PathVariable(value = "ext", required = false) String ext) {
        long sn;
        try {
            sn = Long.parseLong(snStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }
        ImageBlob blob = imageBlobService.loadFull(sn);
        if (blob == null || blob.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(blob.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(blob.getData().length);
        headers.setCacheControl("public, max-age=" + CACHE_SECONDS + ", immutable");
        if (blob.getSha256() != null) {
            headers.setETag("\"" + blob.getSha256() + "\"");
        }
        return new ResponseEntity<>(blob.getData(), headers, org.springframework.http.HttpStatus.OK);
    }
}
