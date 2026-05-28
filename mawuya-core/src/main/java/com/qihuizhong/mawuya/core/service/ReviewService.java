/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.ReviewInfo;
import com.qihuizhong.mawuya.core.mapper.ReviewInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 评论业务层（含审批生命周期）。
 *
 * <p>状态机：</p>
 * <pre>
 *   AMS submit  → status=0 PENDING
 *   BMS approve → status=1 APPROVED  → 前台可见 + article_info.review_num 同步
 *   BMS reject  → status=2 REJECTED  → 前台不可见
 * </pre>
 *
 * @author 钟启辉
 */
@Service
public class ReviewService extends BaseService<ReviewInfo, Integer> {

    /** 评论内容最大长度（与表字段保持一致） */
    public static final int MAX_CONTENT_LENGTH = 500;
    /** 评论者名称最大长度 */
    public static final int MAX_NAME_LENGTH = 40;

    /** 审核状态：待审核 */
    public static final int STATUS_PENDING  = 0;
    /** 审核状态：已通过 */
    public static final int STATUS_APPROVED = 1;
    /** 审核状态：已拒绝 */
    public static final int STATUS_REJECTED = 2;

    private final ReviewInfoMapper reviewInfoMapper;

    @Autowired
    public ReviewService(ReviewInfoMapper baseMapper) {
        super(baseMapper);
        this.reviewInfoMapper = baseMapper;
    }

    /** AMS 文章详情页：仅展示已通过评论（mapper 已带 status=1 过滤） */
    public List<ReviewInfo> listByArticle(Integer articleSn) {
        if (articleSn == null) {
            return Collections.emptyList();
        }
        return reviewInfoMapper.selectByArticleSn(articleSn);
    }

    /** 侧边栏：最新已通过评论（mapper 已带 status=1 过滤） */
    public List<ReviewInfo> listLatest(int n) {
        if (n <= 0) {
            return Collections.emptyList();
        }
        return reviewInfoMapper.selectLatestWithTitle(n);
    }

    /**
     * 提交一条评论；返回错误信息（空字符串表示成功）。
     * 默认 status=PENDING，不立即在前台展示，需 BMS 审批通过后才可见。
     * 提交时不刷新 article_info.review_num（避免给出 inflated 计数）。
     */
    @Transactional
    public String submit(Integer articleSn, String name, String content) {
        if (articleSn == null) {
            return "文章不存在";
        }
        if (StringUtils.isBlank(name)) {
            return "请填写昵称";
        }
        if (StringUtils.isBlank(content)) {
            return "请填写评论内容";
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return "昵称过长（最多 " + MAX_NAME_LENGTH + " 字符）";
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            return "评论内容过长（最多 " + MAX_CONTENT_LENGTH + " 字符）";
        }

        ReviewInfo r = new ReviewInfo()
                .setArticleSn(articleSn)
                .setPsn(0)
                .setReviewName(name.trim())
                .setReviewContent(content.trim())
                .setReviewStatus(STATUS_PENDING);
        if (reviewInfoMapper.insert(r) <= 0) {
            return "评论保存失败";
        }
        // 提交时不动 article_info.review_num —— 它只统计已通过评论
        return "";
    }

    // ----------------- BMS 审批 -----------------

    /** 按状态分页查（带文章标题），status=null 表示全部 */
    public List<ReviewInfo> listByStatus(Integer status, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = (size < 1 || size > 200) ? 20 : size;
        int offset = (safePage - 1) * safeSize;
        return reviewInfoMapper.selectByStatusWithTitle(status, offset, safeSize);
    }

    /** 计数（status=null 表示全部） */
    public int countByStatus(Integer status) {
        return reviewInfoMapper.countByStatus(status);
    }

    /** 审批通过：status→1 + 同步刷新 article_info.review_num */
    @Transactional
    public boolean approve(Integer sn) {
        if (sn == null) return false;
        ReviewInfo r = reviewInfoMapper.selectById(sn);
        if (r == null) return false;
        if (reviewInfoMapper.updateStatus(sn, STATUS_APPROVED) <= 0) {
            return false;
        }
        if (r.getArticleSn() != null) {
            reviewInfoMapper.refreshArticleReviewNum(r.getArticleSn());
        }
        return true;
    }

    /**
     * 审批拒绝：status→2 + 同步刷新 article_info.review_num
     * （从已通过转为已拒绝时，文章计数会减 1；从待审核转为已拒绝时计数不变）
     */
    @Transactional
    public boolean reject(Integer sn) {
        if (sn == null) return false;
        ReviewInfo r = reviewInfoMapper.selectById(sn);
        if (r == null) return false;
        if (reviewInfoMapper.updateStatus(sn, STATUS_REJECTED) <= 0) {
            return false;
        }
        if (r.getArticleSn() != null) {
            reviewInfoMapper.refreshArticleReviewNum(r.getArticleSn());
        }
        return true;
    }
}
