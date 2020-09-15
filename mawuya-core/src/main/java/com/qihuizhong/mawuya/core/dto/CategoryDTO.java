/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2020 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.dto;

import com.qihuizhong.mawuya.core.dto.base.BaseDTO;

/**
 * author: zhongqihui996@gmail.com
 * date: 2020/9/15
 * description: 文章分类实体对象
 */
public class CategoryDTO extends BaseDTO {

    private static final long serialVersionUID = -1168434047828111130L;

    /**
     * 主键
     */
    private String sn;

    /**
     * 分类名称
     */
    private String categoryName;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
