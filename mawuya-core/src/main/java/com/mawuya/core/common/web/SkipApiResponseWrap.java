/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 controller 方法 / 类 <strong>不</strong>参与 {@code BaseResponse} 自动包装。
 *
 * <p>典型场景：</p>
 * <ul>
 *   <li>二进制流（图片、文件）—— 返回 byte[] / ResponseEntity&lt;byte[]&gt;</li>
 *   <li>第三方强约定结构（如 editor.md 的 {@code {success:1,url:''}}）</li>
 *   <li>非 JSON 文本（sitemap.xml / robots.txt / rss.xml）</li>
 *   <li>已经手动返回 {@link com.mawuya.core.common.BaseResponse}（避免双重包装）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipApiResponseWrap {
}
