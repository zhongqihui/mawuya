/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签业务层
 *
 * @author 钟启辉
 */
@Service
public class TagService extends BaseService<Tag, Integer> {

    private final TagMapper tagMapper;

    @Autowired
    public TagService(TagMapper baseMapper) {
        super(baseMapper);
        this.tagMapper = baseMapper;
    }

    public List<Tag> getCloud() {
        return tagMapper.selectAllWithArtSize();
    }

    public List<Tag> getByArticleSn(Integer articleSn) {
        if (articleSn == null) {
            return java.util.Collections.emptyList();
        }
        return tagMapper.selectByArticleSn(articleSn);
    }

    public List<Integer> getArticleSnByTag(Integer tagSn) {
        if (tagSn == null) {
            return java.util.Collections.emptyList();
        }
        return tagMapper.selectArticleSnByTag(tagSn);
    }

    /**
     * 重新绑定文章与标签：先解绑全部，再绑定新的列表
     */
    @Transactional
    public int rebindArticleTags(Integer articleSn, List<Integer> tagSns) {
        if (articleSn == null) {
            return 0;
        }
        tagMapper.unbindByArticle(articleSn);
        if (tagSns == null || tagSns.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (Integer tagSn : tagSns) {
            if (tagSn != null) {
                n += tagMapper.bindArticleTag(articleSn, tagSn);
            }
        }
        return n;
    }
}
