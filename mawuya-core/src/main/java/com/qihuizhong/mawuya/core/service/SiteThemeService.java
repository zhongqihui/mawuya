/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.dataobject.BlogDO;
import com.qihuizhong.mawuya.core.mapper.BlogInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 站点主题（皮肤）服务：BMS 一键换皮的核心业务层。
 *
 * <ul>
 *     <li>读：被 AMS 的 GlobalModelAttributes 每请求注入到视图（blog_info 单行 PK 查询代价极低）</li>
 *     <li>写：被 BMS 的 ThemeController.apply.do 调用，仅当 code 命中白名单时落库</li>
 * </ul>
 *
 * <p>原版皮肤永久作为 {@link #DEFAULT_CODE} 兜底：表损坏 / 字段为空 / 命中非法值 / 抛异常 时
 * {@link #getCurrentTheme()} 一律返回 default，避免前台白屏。</p>
 *
 * @author zqh
 */
@Service
public class SiteThemeService {

    private static final Logger log = LoggerFactory.getLogger(SiteThemeService.class);

    /** 原版主题 code（永久保留，作为 fallback） */
    public static final String DEFAULT_CODE = "default";

    /** 主题白名单（顺序即 BMS 卡片展示顺序） */
    public static final Set<String> ALLOWED_THEMES = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    DEFAULT_CODE,
                    "tech-dark",
                    "gradient-vivid",
                    "minimal-business"
            ))
    );

    private final BlogInfoMapper blogInfoMapper;

    @Autowired
    public SiteThemeService(BlogInfoMapper blogInfoMapper) {
        this.blogInfoMapper = blogInfoMapper;
    }

    /**
     * 获取当前生效主题 code。任意异常 / 空值 / 非法值 → default。
     */
    public String getCurrentTheme() {
        try {
            BlogDO info = blogInfoMapper.selectSingleton();
            if (info == null) {
                return DEFAULT_CODE;
            }
            String code = info.getThemeCode();
            if (code == null || code.trim().isEmpty()) {
                return DEFAULT_CODE;
            }
            return ALLOWED_THEMES.contains(code) ? code : DEFAULT_CODE;
        } catch (Exception e) {
            // 表不存在 / 列不存在 / 连接异常都走 default 兜底，保证前台不白屏
            log.warn("getCurrentTheme failed, fallback to default: {}", e.getMessage());
            return DEFAULT_CODE;
        }
    }

    /**
     * 应用主题。仅白名单 code 才会落库。
     *
     * @return true 表示成功；false 表示 code 非法或更新失败
     */
    public boolean applyTheme(String code) {
        if (code == null || !ALLOWED_THEMES.contains(code)) {
            log.warn("applyTheme rejected, illegal code: {}", code);
            return false;
        }
        try {
            int rows = blogInfoMapper.updateThemeCode(code);
            if (rows <= 0) {
                log.warn("applyTheme update affected 0 rows, code={}", code);
                return false;
            }
            log.info("applyTheme success, code={}", code);
            return true;
        } catch (Exception e) {
            log.error("applyTheme exception, code={}", code, e);
            return false;
        }
    }

    /**
     * 列出所有主题元数据（供 BMS 主题切换页渲染卡片）。
     */
    public List<ThemeMeta> listThemes() {
        List<ThemeMeta> list = new ArrayList<>(ALLOWED_THEMES.size());
        list.add(new ThemeMeta("default",          "原版经典",   "NexT Mist 风格，简洁清爽，与初版视觉一致",        "#3a5fcd", "#ffffff", "#666666"));
        list.add(new ThemeMeta("tech-dark",        "深色科技",   "深空蓝底 + 青色霓虹强调 + 磨砂玻璃卡片",          "#06b6d4", "#0a0e1a", "#7dd3fc"));
        list.add(new ThemeMeta("gradient-vivid",   "活泼渐变",   "紫粉橙渐变背景 + 圆角卡片 + 鲜色按钮",            "#ec4899", "#fef3ff", "#8b5cf6"));
        list.add(new ThemeMeta("minimal-business", "极简商务",   "近白底 + 细线分割 + 灰阶字 + 深蓝单色强调",       "#1e3a8a", "#fafafa", "#6b7280"));
        return list;
    }

    /**
     * 主题元数据，用于 BMS 卡片展示。
     */
    public static class ThemeMeta {
        private final String code;
        private final String name;
        private final String description;
        private final String color1;
        private final String color2;
        private final String color3;

        public ThemeMeta(String code, String name, String description,
                         String color1, String color2, String color3) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getColor1() { return color1; }
        public String getColor2() { return color2; }
        public String getColor3() { return color3; }
    }
}
