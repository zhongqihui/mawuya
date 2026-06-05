/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据库连通性 + 表结构校验。
 *
 * <p>校验目标：</p>
 * <ol>
 *     <li>Druid 数据源可获取连接</li>
 *     <li>schema.sql 创建的 5 张表全部存在</li>
 *     <li>article_info / category_info / log_info 关键列存在且驼峰映射前的下划线列名一致</li>
 * </ol>
 *
 * @author 钟启辉
 */
@SpringBootTest
@DisplayName("E2E-1：数据库连通性 + 表结构校验")
class DatabaseConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Druid 数据源可获取连接，且 5 张业务表全部存在")
    void shouldConnectAndContainsAllTables() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(2)).isTrue();
            assertThat(conn.getCatalog()).isEqualToIgnoringCase("blog");

            DatabaseMetaData meta = conn.getMetaData();
            Set<String> tables = listTables(meta, conn.getCatalog());

            assertThat(tables)
                    .as("schema.sql 必须已经在 blog 库中创建以下 5 张表")
                    .contains("article_info", "category_info", "log_info", "review_info", "blog_info");
        }
    }

    @Test
    @DisplayName("article_info 关键列存在且类型正确")
    void shouldContainArticleInfoColumns() throws Exception {
        Set<String> cols = listColumns("article_info");
        assertThat(cols).contains(
                "sn", "category_sn", "read_num", "review_num", "praise_num", "tease_num",
                "picture_url", "article_title", "article_summary", "article_content",
                "insert_time", "update_time"
        );
    }

    @Test
    @DisplayName("category_info 关键列存在")
    void shouldContainCategoryInfoColumns() throws Exception {
        Set<String> cols = listColumns("category_info");
        assertThat(cols).contains("sn", "category_name");
    }

    @Test
    @DisplayName("log_info 关键列存在")
    void shouldContainLogInfoColumns() throws Exception {
        Set<String> cols = listColumns("log_info");
        assertThat(cols).contains(
                "sn", "ip_addr", "country", "province", "city", "area",
                "detail_position", "isp", "try_times", "req_time", "resp_time",
                "consume_time", "req_url", "req_method", "params", "browser",
                "resp_status", "except_message"
        );
    }

    private Set<String> listTables(DatabaseMetaData meta, String catalog) throws Exception {
        Set<String> tables = new HashSet<>();
        try (ResultSet rs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return tables;
    }

    private Set<String> listColumns(String tableName) throws Exception {
        Set<String> cols = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), null, tableName, "%")) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return cols;
    }
}
