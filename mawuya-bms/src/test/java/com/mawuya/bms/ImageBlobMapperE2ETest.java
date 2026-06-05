/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms;

import com.mawuya.core.dataobject.ImageDO;
import com.mawuya.core.mapper.ImageBlobMapper;
import com.mawuya.core.service.ImageBlobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 图片 DB 存储链路端到端：插入 → 查询 → 去重 → 删除
 *
 * @author 钟启辉
 */
@SpringBootTest
@Transactional
@Rollback
class ImageBlobMapperE2ETest {

    @Autowired
    private ImageBlobMapper mapper;
    @Autowired
    private ImageBlobService service;

    private static final byte[] TINY_PNG = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG sig
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte) 0x9C, 0x63, (byte) 0xFC, (byte) 0xFF, (byte) 0xFF, 0x3F,
            0x03, 0x00, 0x06, 0x05, 0x02, 0x00, (byte) 0xA0, (byte) 0xB1, 0x4A, (byte) 0xED,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Test
    @DisplayName("插入 + 元信息查询 + 完整字节读取")
    void insertAndQuery() {
        long sn = service.save(TINY_PNG, "tiny.png", "image/png", null);
        assertThat(sn).isPositive();

        ImageDO meta = mapper.selectMetaById(sn);
        assertThat(meta).isNotNull();
        assertThat(meta.getFileName()).isEqualTo("tiny.png");
        assertThat(meta.getContentType()).isEqualTo("image/png");
        assertThat(meta.getByteSize()).isEqualTo((long) TINY_PNG.length);
        assertThat(meta.getData()).isNull(); // metaMap 不带 data
        assertThat(meta.getSha256()).hasSize(64);

        ImageDO full = mapper.selectFullById(sn);
        assertThat(full.getData()).isEqualTo(TINY_PNG);
    }

    @Test
    @DisplayName("相同字节自动按 sha256 去重，复用同一个 sn")
    void shouldDedupBySha256() {
        long sn1 = service.save(TINY_PNG, "a.png", "image/png", "https://x.com/a.png");
        long sn2 = service.save(TINY_PNG, "b.png", "image/png", "https://x.com/b.png");
        assertThat(sn2).isEqualTo(sn1);
    }

    @Test
    @DisplayName("通过 sourceUrl 反查 sn：迁移工具用")
    void findBySourceUrl() {
        long sn = service.save(TINY_PNG, "u.png", "image/png", "https://example.com/u.png");
        Long found = service.getSnBySourceUrl("https://example.com/u.png");
        assertThat(found).isEqualTo(sn);
    }

    @Test
    @DisplayName("删除按 sn")
    void delete() {
        long sn = service.save(TINY_PNG, "del.png", "image/png", null);
        boolean ok = service.removeById(sn);
        assertThat(ok).isTrue();
        assertThat(mapper.selectMetaById(sn)).isNull();
    }
}
