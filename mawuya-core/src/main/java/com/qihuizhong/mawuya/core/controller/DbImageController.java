/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.controller;

import com.qihuizhong.mawuya.core.common.web.SkipApiResponseWrap;
import com.qihuizhong.mawuya.core.dto.request.ImageFetchRequest;
import com.qihuizhong.mawuya.core.dataobject.ImageDO;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
 * <h3>API 规范化说明</h3>
 * <ul>
 *   <li><strong>入参 DTO 化</strong>：使用 {@link ImageFetchRequest} 承接路径参数，做格式校验（sn 必须数字、ext 仅字母）。</li>
 *   <li><strong>返回保留 byte[]</strong>：本接口被 markdown {@code <img src>} 直接消费，必须返回 raw bytes 与正确的
 *       Content-Type；这里通过类级 {@link SkipApiResponseWrap} 显式排除 {@code BaseResponse} 自动包装。</li>
 *   <li><strong>异常分支</strong>：sn 解析失败 → 400；记录不存在 → 404；正常输出 200 + 30 天强缓存 + sha256 ETag。</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("image/db")
@SkipApiResponseWrap
public class DbImageController {

    /** 浏览器缓存 30 天（图片内容不可变，因为 sha256 唯一） */
    private static final long CACHE_SECONDS = 30L * 24 * 3600;

    @Autowired
    private ImageBlobService imageBlobService;

    /**
     * 形如 /image/db/123、/image/db/123.png、/image/db/123.jpg 都可命中。
     *
     * @param snStr 路径变量 sn，调用方可能带后缀也可能不带
     * @param ext   可选扩展名，仅做兼容，不参与图片真正的解析（解析以 image_blob.contentType 为准）
     */
    @GetMapping({"/{sn}", "/{sn}.{ext}"})
    public ResponseEntity<byte[]> get(@PathVariable("sn") String snStr,
                                      @PathVariable(value = "ext", required = false) String ext) {
        // 1) 入参 DTO 化 + 校验：把"裸字符串"立即收敛进受控对象
        ImageFetchRequest req = new ImageFetchRequest(snStr, ext);
        if (!isSnValid(req.getSn())) {
            return ResponseEntity.badRequest().build();
        }

        // 2) 业务查询
        ImageDO blob = imageBlobService.getFullById(req.snAsLong());
        if (blob == null || blob.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        // 3) Content-Type 解析：库内类型为准；解析失败兜底为 application/octet-stream
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(blob.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // 4) 强缓存 + ETag
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(blob.getData().length);
        headers.setCacheControl("public, max-age=" + CACHE_SECONDS + ", immutable");
        if (blob.getSha256() != null) {
            headers.setETag("\"" + blob.getSha256() + "\"");
        }
        return new ResponseEntity<>(blob.getData(), headers, HttpStatus.OK);
    }

    /**
     * 简化版 sn 校验：仅允许 1~19 位数字（避免在视图链路引入完整 javax.validation 触发器）。
     * 不合法直接 400，业务层永远拿到合法 long。
     */
    private static boolean isSnValid(String sn) {
        if (sn == null || sn.isEmpty() || sn.length() > 19) return false;
        for (int i = 0; i < sn.length(); i++) {
            char c = sn.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
}
