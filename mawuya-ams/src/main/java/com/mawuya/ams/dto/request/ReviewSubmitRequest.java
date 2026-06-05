/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * AMS 评论提交请求 DTO（{@code POST /comments/submit}）。
 *
 * <p>表单提交场景：JS 框架/手写 form 都按 form-urlencoded 提交，Spring 用 setter 注入到本 DTO。
 * 校验规则与 ReviewService 内部规则保持一致：</p>
 *
 * <ul>
 *   <li>articleSn 必填</li>
 *   <li>name     必填，最多 40 字符</li>
 *   <li>content  必填，最多 500 字符</li>
 * </ul>
 *
 * <p>本接口走 {@code redirect:} 跳转，不返回 JSON；service 层会再做一次校验并把
 * 友好的错误文字塞 flash attribute 回到详情页底部展示。</p>
 *
 * @author 钟启辉
 */
public class ReviewSubmitRequest {

    @NotNull(message = "articleSn 不能为空")
    private Integer articleSn;

    @NotBlank(message = "请填写昵称")
    @Size(max = 40, message = "昵称最长 40 字符")
    private String name;

    @NotBlank(message = "请填写评论内容")
    @Size(max = 500, message = "评论内容最长 500 字符")
    private String content;

    public Integer getArticleSn() { return articleSn; }
    public ReviewSubmitRequest setArticleSn(Integer articleSn) { this.articleSn = articleSn; return this; }

    public String getName() { return name; }
    public ReviewSubmitRequest setName(String name) { this.name = name; return this; }

    public String getContent() { return content; }
    public ReviewSubmitRequest setContent(String content) { this.content = content; return this; }
}
