/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms;

import com.qihuizhong.mawuya.core.dataobject.ArticleDO;
import com.qihuizhong.mawuya.core.dataobject.TagDO;
import com.qihuizhong.mawuya.core.mapper.TagMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 标签 Mapper 端到端测试。
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@DisplayName("E2E-5：TagMapper 全功能（CRUD + 关联绑定 + 统计）")
class TagMapperE2ETest {

    @Autowired private TagMapper tagMapper;

    @Test
    @DisplayName("CRUD：insert / selectById / update / deleteById")
    void shouldRunFullCrud() {
        String name = "ut-tag-" + UUID.randomUUID().toString().substring(0, 8);
        TagDO t = new TagDO(name).setTagColor("#abcdef");
        assertThat(tagMapper.insert(t)).isPositive();

        TagDO saved = tagMapper.selectList(new HashMap<>()).stream()
                .filter(x -> name.equals(x.getTagName())).findFirst().orElseThrow(() -> new AssertionError("未找到刚插入的标签: " + name));
        assertThat(saved.getSn()).isPositive();
        assertThat(saved.getTagColor()).isEqualTo("#abcdef");

        saved.setTagColor("#123456");
        assertThat(tagMapper.update(saved)).isEqualTo(1);
        assertThat(tagMapper.selectById(saved.getSn()).getTagColor()).isEqualTo("#123456");

        assertThat(tagMapper.deleteById(saved.getSn())).isEqualTo(1);
        assertThat(tagMapper.selectById(saved.getSn())).isNull();
    }

    @Test
    @DisplayName("文章-标签 双向关联：bind / selectByArticleSn / selectArticleSnByTag / unbind")
    void shouldBindAndQueryArticleTag() {
        // 准备一个标签
        String name = "ut-bind-" + UUID.randomUUID().toString().substring(0, 6);
        tagMapper.insert(new TagDO(name).setTagColor("#999"));
        TagDO tag = tagMapper.selectList(new HashMap<>()).stream()
                .filter(x -> name.equals(x.getTagName())).findFirst().orElseThrow(() -> new AssertionError("未找到刚插入的标签: " + name));

        Integer articleSn = 9999;  // 虚拟文章 sn，本测试只验证关联表行为
        assertThat(tagMapper.bindArticleTag(articleSn, tag.getSn())).isEqualTo(1);

        // 反查
        List<TagDO> tags = tagMapper.selectByArticleSn(articleSn);
        assertThat(tags).extracting(TagDO::getTagName).contains(name);

        List<Integer> articleSns = tagMapper.selectArticleSnByTag(tag.getSn());
        assertThat(articleSns).contains(articleSn);

        // 重复 bind 不会重复（INSERT IGNORE）
        assertThat(tagMapper.bindArticleTag(articleSn, tag.getSn())).isEqualTo(0);

        // 解绑
        assertThat(tagMapper.unbindByArticle(articleSn)).isEqualTo(1);
        assertThat(tagMapper.selectByArticleSn(articleSn)).isEmpty();
    }

    @Test
    @DisplayName("selectAllWithArtSize：标签云 art_size 计数正确")
    void shouldComputeArtSize() {
        List<TagDO> cloud = tagMapper.selectAllWithArtSize();
        assertThat(cloud).isNotEmpty();
        // 每个 TagDO 都应有 artSize（≥0）
        assertThat(cloud).allSatisfy(t -> {
            assertThat(t.getSn()).isPositive();
            assertThat(t.getTagName()).isNotBlank();
            assertThat(t.getArtSize()).isNotNull().isNotNegative();
        });
    }
}
