/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2020 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.dto.base;

import java.io.Serializable;
import java.util.Date;

/**
 * author: zhongqihui996@gmail.com
 * date: 2020/9/15
 * description: DTO 基类，所有DTO应该继承该DTO
 */
public class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1384279646018893620L;

    /**
     * 创建者
     */
    private String inputBy;

    /**
     * 创建时间
     */
    private Date inputDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    public String getInputBy() {
        return inputBy;
    }

    public void setInputBy(String inputBy) {
        this.inputBy = inputBy;
    }

    public Date getInputDate() {
        return inputDate;
    }

    public void setInputDate(Date inputDate) {
        this.inputDate = inputDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
