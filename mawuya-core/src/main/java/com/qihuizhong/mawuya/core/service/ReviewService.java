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

import java.util.List;

/**
 * 评论业务层
 *
 * @author 钟启辉
 */
@Service
public class ReviewService extends BaseService<ReviewInfo, Integer> {

    /** 评论内容最大长度（与表字段保持一致） */
    public static final int MAX_CONTENT_LENGTH = 500;
    /** 评论者名称最大长度 */
    public static final int MAX_NAME_LENGTH = 40;

    private final ReviewInfoMapper reviewInfoMapper;

    @Autowired
    public ReviewService(ReviewInfoMapper baseMapper) {
        super(baseMapper);
        this.reviewInfoMapper = baseMapper;
    }

    public List<ReviewInfo> listByArticle(Integer articleSn) {
        if (articleSn == null) {
            return java.util.Collections.emptyList();
        }
        return reviewInfoMapper.selectByArticleSn(articleSn);
    }

    public List<ReviewInfo> listLatest(int n) {
        if (n <= 0) {
            return java.util.Collections.emptyList();
        }
        return reviewInfoMapper.selectLatestWithTitle(n);
    }

    /**
     * 提交一条评论；返回错误信息（空字符串表示成功）。
     * 同步刷新 article_info.review_num。
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
                .setReviewContent(content.trim());
        if (reviewInfoMapper.insert(r) <= 0) {
            return "评论保存失败";
        }
        reviewInfoMapper.refreshArticleReviewNum(articleSn);
        return "";
    }
}
