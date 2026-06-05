/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms;

import com.niudeyapi.mawuya.core.dataobject.CategoryDO;
import com.niudeyapi.mawuya.core.mapper.CategoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分类 Mapper 端到端 CRUD 测试。
 *
 * <p>使用 {@code @Transactional} + {@code @Rollback}，所有写操作在测试结束后自动回滚，不污染数据。</p>
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E-2：CategoryMapper CRUD 链路")
class CategoryMapperE2ETest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    @Order(1)
    @DisplayName("insert -> selectById -> update -> deleteById 全链路")
    void shouldRunFullCrud() {
        // 1. insert
        String name = "ut-cat-" + UUID.randomUUID().toString().substring(0, 8);
        CategoryDO c = new CategoryDO().setCategoryName(name);
        int rows = categoryMapper.insert(c);
        assertThat(rows).isEqualTo(1);

        // 2. 通过 selectList 反查（insert 没有返回主键）
        List<CategoryDO> all = categoryMapper.selectList(new HashMap<>());
        CategoryDO saved = all.stream()
                .filter(x -> name.equals(x.getCategoryName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("新增分类未被查询到"));
        assertThat(saved.getSn()).isNotNull().isPositive();

        // 3. selectById
        CategoryDO got = categoryMapper.selectById(saved.getSn());
        assertThat(got).isNotNull();
        assertThat(got.getCategoryName()).isEqualTo(name);

        // 4. update
        String newName = name + "-U";
        got.setCategoryName(newName);
        int updated = categoryMapper.update(got);
        assertThat(updated).isEqualTo(1);
        assertThat(categoryMapper.selectById(got.getSn()).getCategoryName()).isEqualTo(newName);

        // 5. delete
        int deleted = categoryMapper.deleteById(got.getSn());
        assertThat(deleted).isEqualTo(1);
        assertThat(categoryMapper.selectById(got.getSn())).isNull();
    }

    @Test
    @Order(2)
    @DisplayName("selectByPage 分页参数 start/limit 生效")
    void shouldPaginate() {
        // 准备 5 条记录
        for (int i = 0; i < 5; i++) {
            categoryMapper.insert(new CategoryDO()
                    .setCategoryName("page-cat-" + UUID.randomUUID().toString().substring(0, 6)));
        }

        Map<String, Object> p = new HashMap<>();
        p.put("start", 0);
        p.put("limit", 3);
        List<CategoryDO> page = categoryMapper.selectByPage(p);
        assertThat(page).hasSize(3);

        Integer count = categoryMapper.selectCount(new HashMap<>());
        assertThat(count).isGreaterThanOrEqualTo(5);
    }
}
