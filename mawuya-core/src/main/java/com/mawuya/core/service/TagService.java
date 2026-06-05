/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.service;

import com.mawuya.core.dataobject.TagDO;
import com.mawuya.core.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 标签业务层。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code list/save/update/remove}。</p>
 *
 * @author 钟启辉
 */
@Service
public class TagService extends BaseService<TagDO, Integer> {

    private final TagMapper tagMapper;

    @Autowired
    public TagService(TagMapper baseMapper) {
        super(baseMapper);
        this.tagMapper = baseMapper;
    }

    /** 列出标签云（带文章计数）。 */
    public List<TagDO> listTagCloud() {
        return tagMapper.selectAllWithArtSize();
    }

    /** 列出某文章关联的所有标签。 */
    public List<TagDO> listByArticleSn(Integer articleSn) {
        if (articleSn == null) {
            return Collections.emptyList();
        }
        return tagMapper.selectByArticleSn(articleSn);
    }

    /** 列出某标签下的文章 sn 列表。 */
    public List<Integer> listArticleSnByTag(Integer tagSn) {
        if (tagSn == null) {
            return Collections.emptyList();
        }
        return tagMapper.selectArticleSnByTag(tagSn);
    }

    /**
     * 重新绑定文章与标签：先解绑全部，再绑定新的列表。
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
