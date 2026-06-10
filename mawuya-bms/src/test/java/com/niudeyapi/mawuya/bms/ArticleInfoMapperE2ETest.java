/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms;

import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.mapper.ArticleInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文章 Mapper 端到端 CRUD 测试。
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E-3：ArticleInfoMapper 全功能")
class ArticleInfoMapperE2ETest {

    @Autowired
    private ArticleInfoMapper articleMapper;

    private ArticleDO newArticle(String title, int categorySn) {
        return new ArticleDO()
                .setCategorySn(categorySn)
                .setReadNum(0)
                .setReviewNum(0)
                .setPraiseNum(0)
                .setTeaseNum(0)
                .setPictureUrl("/upload/test.png")
                .setArticleTitle(title)
                .setArticleSummary("summary-" + title)
                .setArticleContent("content-" + title)
                // mapper.xml 的 INSERT 使用 #{status}，不设的话会写入 NULL；
                // selectPrev/Next 限定 status=1，所以默认按已发布插入
                .setStatus(1);
    }

    /** 通过标题反查最新插入的文章主键。insert 语句没有 useGeneratedKeys，故只能这样回查。 */
    private Integer findIdByTitle(String title) {
        List<ArticleDO> list = articleMapper.selectList(new HashMap<>());
        return list.stream()
                .filter(a -> title.equals(a.getArticleTitle()))
                .findFirst()
                .map(ArticleDO::getSn)
                .orElseThrow(() -> new AssertionError("未找到刚插入的文章: " + title));
    }

    @Test
    @Order(1)
    @DisplayName("insert -> selectById -> update -> updatePictureUrl -> deleteById")
    void shouldRunFullCrud() {
        // insert
        String title = "ut-art-" + UUID.randomUUID().toString().substring(0, 8);
        assertThat(articleMapper.insert(newArticle(title, 0))).isEqualTo(1);

        Integer sn = findIdByTitle(title);
        assertThat(sn).isPositive();

        // selectById：内容字段也要被读出来
        ArticleDO got = articleMapper.selectById(sn);
        assertThat(got).isNotNull();
        assertThat(got.getArticleTitle()).isEqualTo(title);
        assertThat(got.getArticleContent()).isEqualTo("content-" + title);
        assertThat(got.getPictureUrl()).isEqualTo("/upload/test.png");

        // update：改标题与内容
        got.setArticleTitle(title + "-U")
           .setArticleContent("content-updated")
           .setReadNum(10);
        assertThat(articleMapper.update(got)).isEqualTo(1);

        ArticleDO updated = articleMapper.selectById(sn);
        assertThat(updated.getArticleTitle()).isEqualTo(title + "-U");
        assertThat(updated.getArticleContent()).isEqualTo("content-updated");
        assertThat(updated.getReadNum()).isEqualTo(10);

        // updatePictureUrl：仅更新背景图字段
        ArticleDO patch = new ArticleDO().setSn(sn).setPictureUrl("/upload/new.png");
        assertThat(articleMapper.updatePictureUrl(patch)).isEqualTo(1);
        assertThat(articleMapper.selectById(sn).getPictureUrl()).isEqualTo("/upload/new.png");

        // delete
        assertThat(articleMapper.deleteById(sn)).isEqualTo(1);
        assertThat(articleMapper.selectById(sn)).isNull();
    }

    @Test
    @Order(2)
    @DisplayName("selectAllNoContent / selectByPage / selectCount 按 categorySn 过滤")
    void shouldQueryByCategory() {
        int cat = 9999;
        for (int i = 0; i < 4; i++) {
            articleMapper.insert(newArticle("cat-art-" + i + "-" + UUID.randomUUID(), cat));
        }
        // 另一个分类下也插一条干扰数据
        articleMapper.insert(newArticle("other-art-" + UUID.randomUUID(), 8888));

        // mapper.xml OGNL 使用 StringUtils#isNotEmpty(CharSequence)，业务调用方
        // （IndexController）也都是从 HTTP 参数取得字符串，故此处必须以 String 传入。
        Map<String, Object> p = new HashMap<>();
        p.put("categorySn", String.valueOf(cat));

        List<ArticleDO> noContent = articleMapper.selectAllNoContent(p);
        assertThat(noContent).hasSize(4);
        // 摘要列表不应携带正文
        assertThat(noContent).allSatisfy(a -> assertThat(a.getArticleContent()).isNull());

        p.put("start", 0);
        p.put("limit", 2);
        List<ArticleDO> page = articleMapper.selectByPage(p);
        assertThat(page).hasSize(2);

        p.remove("start");
        p.remove("limit");
        Integer cnt = articleMapper.selectCount(p);
        assertThat(cnt).isEqualTo(4);
    }

    @Test
    @Order(3)
    @DisplayName("selectPrevById / selectNextById：上一篇下一篇逻辑")
    void shouldFindPrevAndNext() {
        String t1 = "nav-" + UUID.randomUUID();
        String t2 = "nav-" + UUID.randomUUID();
        String t3 = "nav-" + UUID.randomUUID();
        articleMapper.insert(newArticle(t1, 0));
        Integer sn1 = findIdByTitle(t1);
        articleMapper.insert(newArticle(t2, 0));
        Integer sn2 = findIdByTitle(t2);
        articleMapper.insert(newArticle(t3, 0));
        Integer sn3 = findIdByTitle(t3);

        // sn1 < sn2 < sn3
        assertThat(sn1).isLessThan(sn2);
        assertThat(sn2).isLessThan(sn3);

        ArticleDO next = articleMapper.selectNextById(sn1);
        assertThat(next).isNotNull();
        assertThat(next.getSn()).isEqualTo(sn2);

        ArticleDO prev = articleMapper.selectPrevById(sn3);
        assertThat(prev).isNotNull();
        assertThat(prev.getSn()).isEqualTo(sn2);
    }

    @Test
    @Order(4)
    @DisplayName("updateBatchReadNum：批量阅读数 +1")
    void shouldBatchIncreaseReadNum() {
        String t1 = "rd-" + UUID.randomUUID();
        String t2 = "rd-" + UUID.randomUUID();
        articleMapper.insert(newArticle(t1, 0));
        articleMapper.insert(newArticle(t2, 0));
        Integer sn1 = findIdByTitle(t1);
        Integer sn2 = findIdByTitle(t2);

        // 注意：${value} 直接拼 SQL，因此入参严格限定为 \d+(,\d+)*
        String sns = sn1 + "," + sn2;
        int rows = articleMapper.updateBatchReadNum(sns);
        assertThat(rows).isEqualTo(2);

        assertThat(articleMapper.selectById(sn1).getReadNum()).isEqualTo(1);
        assertThat(articleMapper.selectById(sn2).getReadNum()).isEqualTo(1);
    }

    @Test
    @Order(5)
    @DisplayName("updateBatchCategorySn：分类删除时把相关文章的 categorySn 重置为 0")
    void shouldBatchResetCategorySn() {
        int cat = 7777;
        String t1 = "cs-" + UUID.randomUUID();
        String t2 = "cs-" + UUID.randomUUID();
        articleMapper.insert(newArticle(t1, cat));
        articleMapper.insert(newArticle(t2, cat));

        List<Integer> sns = Arrays.asList(findIdByTitle(t1), findIdByTitle(t2));
        String csv = sns.stream().map(String::valueOf).collect(Collectors.joining(","));

        int rows = articleMapper.updateBatchCategorySn(csv);
        assertThat(rows).isEqualTo(2);

        for (Integer sn : sns) {
            assertThat(articleMapper.selectById(sn).getCategorySn()).isEqualTo(0);
        }
    }
}
