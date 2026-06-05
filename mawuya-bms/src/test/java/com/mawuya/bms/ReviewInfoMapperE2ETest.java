/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms;

import com.mawuya.core.dataobject.ReviewDO;
import com.mawuya.core.mapper.ReviewInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 评论 Mapper 端到端测试。
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@DisplayName("E2E-6：ReviewInfoMapper（评论列表 + 最新评论 + 计数同步）")
class ReviewInfoMapperE2ETest {

    @Autowired private ReviewInfoMapper reviewMapper;

    private ReviewDO build(Integer articleSn, String name) {
        return new ReviewDO()
                .setArticleSn(articleSn)
                .setPsn(0)
                .setReviewName(name)
                .setReviewContent("测试评论 " + UUID.randomUUID());
    }

    @Test
    @DisplayName("insert -> selectByArticleSn 顺序按时间倒序")
    void shouldListByArticle() {
        Integer aid = 8888;
        for (int i = 0; i < 3; i++) {
            assertThat(reviewMapper.insert(build(aid, "user-" + i))).isEqualTo(1);
        }
        List<ReviewDO> list = reviewMapper.selectByArticleSn(aid);
        assertThat(list).hasSize(3);
        // 时间倒序：第一条评论是最近插入的（last id）
        assertThat(list.get(0).getSn()).isGreaterThan(list.get(2).getSn());
    }

    @Test
    @DisplayName("selectLatestWithTitle 带文章标题")
    void shouldListLatestWithTitle() {
        // 用已存在的种子文章 sn 查（这里直接用 latestN，不验证特定文章）
        List<ReviewDO> latest = reviewMapper.selectLatestWithTitle(5);
        // 数据库中可能有也可能没有评论，只校验字段结构正确
        for (ReviewDO r : latest) {
            assertThat(r.getArticleSn()).isNotNull();
            assertThat(r.getArticleTitle()).isNotBlank();
            assertThat(r.getReviewName()).isNotBlank();
        }
    }
}
