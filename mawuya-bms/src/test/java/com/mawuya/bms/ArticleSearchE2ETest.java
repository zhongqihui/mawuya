/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms;

import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.mapper.ArticleInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全文搜索 / 批量查询 / 热门 Top N 端到端测试。
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@DisplayName("E2E-7：ArticleDO 搜索与批量查询")
class ArticleSearchE2ETest {

    @Autowired private ArticleInfoMapper articleMapper;

    private ArticleDO build(String title, String content) {
        return new ArticleDO()
                .setCategorySn(0).setReadNum(0).setReviewNum(0)
                .setPraiseNum(0).setTeaseNum(0)
                .setArticleTitle(title).setArticleSummary(title + " summary")
                .setArticleContent(content);
    }

    @Test
    @DisplayName("searchByKeyword：标题/摘要/正文 任一命中即返回")
    void shouldSearchAcrossFields() {
        String tag = "k-" + UUID.randomUUID().toString().substring(0, 8);
        articleMapper.insert(build("ut-search-1 " + tag,                     "正文 hello"));
        articleMapper.insert(build("ut-search-2 normal",                      "正文里包含 " + tag));
        articleMapper.insert(build("ut-search-3 normal",                      "summary " + tag + " here"));
        articleMapper.insert(build("ut-search-4 noise",                       "无关正文"));

        List<ArticleDO> hits = articleMapper.searchByKeyword(tag, 0, 10);
        // 4 条中有 3 条命中（任一字段含 tag）
        assertThat(hits).hasSize(3);

        Integer count = articleMapper.countByKeyword(tag);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("selectListBySnList：按 sn 列表批量取（不带正文）")
    void shouldListBySns() {
        articleMapper.insert(build("ut-batch-A " + UUID.randomUUID(), "ca"));
        articleMapper.insert(build("ut-batch-B " + UUID.randomUUID(), "cb"));

        List<ArticleDO> two = articleMapper.selectAllNoContent(new java.util.HashMap<>()).stream()
                .filter(a -> a.getArticleTitle().startsWith("ut-batch-"))
                .collect(java.util.stream.Collectors.toList());
        assertThat(two).hasSizeGreaterThanOrEqualTo(2);

        List<Integer> sns = Arrays.asList(two.get(0).getSn(), two.get(1).getSn());
        List<ArticleDO> picked = articleMapper.selectListBySnList(sns);
        assertThat(picked).extracting(ArticleDO::getSn).containsAll(sns);
        // _columns_no_content 不包含 articleContent
        assertThat(picked).allSatisfy(a -> assertThat(a.getArticleContent()).isNull());
    }

    @Test
    @DisplayName("selectHotTopN：按 read_num 倒序")
    void shouldListHot() {
        articleMapper.insert(build("ut-hot-low " + UUID.randomUUID(),  "x").setReadNum(3));
        articleMapper.insert(build("ut-hot-mid " + UUID.randomUUID(),  "x").setReadNum(99));
        articleMapper.insert(build("ut-hot-high " + UUID.randomUUID(), "x").setReadNum(9999));

        List<ArticleDO> top = articleMapper.selectHotTopN(3);
        assertThat(top).hasSize(3);
        // 严格递减
        assertThat(top.get(0).getReadNum()).isGreaterThanOrEqualTo(top.get(1).getReadNum());
        assertThat(top.get(1).getReadNum()).isGreaterThanOrEqualTo(top.get(2).getReadNum());
    }
}
