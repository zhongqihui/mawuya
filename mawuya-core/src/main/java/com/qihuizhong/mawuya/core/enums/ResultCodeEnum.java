/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2020 钟启辉. All Rights Reserved.
 */

package com.qihuizhong.mawuya.core.enums;

/**
 * author: zhongqihui996@gmail.com
 * date: 2020/9/15
 * description: 全局结果码枚举定义
 */
public enum ResultCodeEnum {

    SUCCESS("000000", "成功"),
    SYSTEM_ERR("000500", "系统异常，请联系zhongqihui996@gmail.com"),
    ;

    private String resultCode;

    private String resultMessage;

    ResultCodeEnum(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
