/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 图片二进制查询入参 DTO。
 *
 * <p>承接 {@code GET /image/db/{sn}} 与 {@code GET /image/db/{sn}.{ext}} 两条路由的路径参数，
 * 在 controller 边界完成基础校验，业务层只与"已校验对象"打交道。</p>
 *
 * <p>校验规则：</p>
 * <ul>
 *   <li>{@code sn} 必填，仅允许 1~19 位数字（涵盖 long 的合法十进制位数）</li>
 *   <li>{@code ext} 选填，最多 8 个英文字母（兼容 jpg/jpeg/png/webp 等扩展名，不参与解析图片）</li>
 * </ul>
 *
 * @author 钟启辉
 */
public class ImageFetchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "sn 不能为空")
    @Pattern(regexp = "^\\d{1,19}$", message = "sn 必须是数字")
    private String sn;

    @Pattern(regexp = "^[A-Za-z]{0,8}$", message = "扩展名格式不合法")
    private String ext;

    public ImageFetchRequest() {
    }

    public ImageFetchRequest(String sn, String ext) {
        this.sn = sn;
        this.ext = ext;
    }

    /** 解析为 long sn；外部应在调用前确保 {@link #sn} 已通过格式校验 */
    public long snAsLong() {
        return Long.parseLong(sn);
    }

    public String getSn() { return sn; }
    public ImageFetchRequest setSn(String sn) { this.sn = sn; return this; }

    public String getExt() { return ext; }
    public ImageFetchRequest setExt(String ext) { this.ext = ext; return this; }
}
