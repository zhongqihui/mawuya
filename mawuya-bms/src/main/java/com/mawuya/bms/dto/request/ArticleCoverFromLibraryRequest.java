/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 从图片库挑选图片设为文章封面（{@code POST /bms/api/article/cover/from-library}）。
 *
 * @author 钟启辉
 */
public class ArticleCoverFromLibraryRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotNull(message = "imageSn 不能为空")
    @Positive(message = "imageSn 必须为正数")
    private Long imageSn;

    public Integer getSn() { return sn; }
    public ArticleCoverFromLibraryRequest setSn(Integer sn) { this.sn = sn; return this; }

    public Long getImageSn() { return imageSn; }
    public ArticleCoverFromLibraryRequest setImageSn(Long imageSn) { this.imageSn = imageSn; return this; }
}
