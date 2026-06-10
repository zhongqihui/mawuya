/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 文章状态流转日志 DO（对应表 {@code article_status_log}）。
 *
 * <p>每一次「草稿 → 发布 / 已发布 → 撤回 / 已撤回 → 重新发布 / 草稿更新」都会写一行，
 * 用于事后审计、回溯文章生命周期。</p>
 *
 * @author 钟启辉
 */
public class ArticleStatusLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long sn;
    /** 关联文章 sn */
    private Integer articleSn;
    /** 变更前状态（首次新建草稿为 null） */
    private Integer fromStatus;
    /** 变更后状态 */
    private Integer toStatus;
    /** 动作：CREATE_DRAFT / PUBLISH / UPDATE_DRAFT / WITHDRAW / REPUBLISH / UPDATE_PUBLISHED */
    private String action;
    /** 操作人（取 BMS Spring Security 当前登录用户 username，匿名时为 null） */
    private String operator;
    /** 备注（可空） */
    private String remark;
    /** 发生时间（DB 自动填默认） */
    private String changeTime;

    public Long getSn() { return sn; }
    public ArticleStatusLogDO setSn(Long sn) { this.sn = sn; return this; }

    public Integer getArticleSn() { return articleSn; }
    public ArticleStatusLogDO setArticleSn(Integer articleSn) { this.articleSn = articleSn; return this; }

    public Integer getFromStatus() { return fromStatus; }
    public ArticleStatusLogDO setFromStatus(Integer fromStatus) { this.fromStatus = fromStatus; return this; }

    public Integer getToStatus() { return toStatus; }
    public ArticleStatusLogDO setToStatus(Integer toStatus) { this.toStatus = toStatus; return this; }

    public String getAction() { return action; }
    public ArticleStatusLogDO setAction(String action) { this.action = action; return this; }

    public String getOperator() { return operator; }
    public ArticleStatusLogDO setOperator(String operator) { this.operator = operator; return this; }

    public String getRemark() { return remark; }
    public ArticleStatusLogDO setRemark(String remark) { this.remark = remark; return this; }

    public String getChangeTime() { return changeTime; }
    public ArticleStatusLogDO setChangeTime(String changeTime) { this.changeTime = changeTime; return this; }
}
