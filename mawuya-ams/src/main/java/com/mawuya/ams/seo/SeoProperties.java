/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.seo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AMS 全站 SEO 配置项，统一在 application.yml 的 {@code mawuya.seo.*} 下维护。
 *
 * <p>所有 TDK / Canonical / OG / Sitemap / Robots 都从这里读取站点级默认值，
 * 页面级数据再由各 Controller 通过 {@link SeoModel} 覆盖。</p>
 *
 * @author 钟启辉
 */
@Component
@ConfigurationProperties(prefix = "mawuya.seo")
public class SeoProperties {

    /** 站点对外可访问的根 URL（必须含 scheme，不带尾斜杠），用于拼接绝对地址 / canonical / sitemap loc */
    private String siteUrl = "http://localhost:8080";
    /** 站点显示名（OG site_name、JSON-LD WebSite name） */
    private String siteName = "Zqh's Blog";
    /** 站点默认标题（首页 title 后缀） */
    private String defaultTitle = "Zqh's Blog - 分享技术与生活";
    /** 站点默认描述（meta description 兜底） */
    private String defaultDescription = "Mawuya Blog - 一个分享 Java、Spring Boot、MySQL 等后端技术与生活随笔的小站。";
    /** 站点默认关键字（meta keywords 兜底） */
    private String defaultKeywords = "Java,Spring Boot,MySQL,Mybatis,博客,技术分享";
    /** 默认 OG / Twitter 卡片缩略图（绝对路径或带 / 的相对路径） */
    private String defaultOgImage = "/statics/images/avatars/avatar2.jpg";
    /** 站点作者名（schema.org Person.name、author meta） */
    private String author = "Zhongqihui";
    /** 默认语言（lang / og:locale） */
    private String locale = "zh_CN";
    /** Twitter 卡片类型：summary / summary_large_image */
    private String twitterCard = "summary_large_image";
    /** Twitter 账户名（可选，不填则不输出 twitter:site） */
    private String twitterSite = "";
    /** 是否允许搜索引擎收录（生产环境 true，预发/测试 false） */
    private boolean robotsIndex = true;
    /** sitemap 单文件最多包含的 URL 数（标准 5w 上限，博客远低于此） */
    private int sitemapMaxUrls = 5000;

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) {
        // 去掉尾斜杠，便于直接拼 path
        this.siteUrl = (siteUrl != null && siteUrl.endsWith("/"))
                ? siteUrl.substring(0, siteUrl.length() - 1)
                : siteUrl;
    }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getDefaultTitle() { return defaultTitle; }
    public void setDefaultTitle(String defaultTitle) { this.defaultTitle = defaultTitle; }

    public String getDefaultDescription() { return defaultDescription; }
    public void setDefaultDescription(String defaultDescription) { this.defaultDescription = defaultDescription; }

    public String getDefaultKeywords() { return defaultKeywords; }
    public void setDefaultKeywords(String defaultKeywords) { this.defaultKeywords = defaultKeywords; }

    public String getDefaultOgImage() { return defaultOgImage; }
    public void setDefaultOgImage(String defaultOgImage) { this.defaultOgImage = defaultOgImage; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getTwitterCard() { return twitterCard; }
    public void setTwitterCard(String twitterCard) { this.twitterCard = twitterCard; }

    public String getTwitterSite() { return twitterSite; }
    public void setTwitterSite(String twitterSite) { this.twitterSite = twitterSite; }

    public boolean isRobotsIndex() { return robotsIndex; }
    public void setRobotsIndex(boolean robotsIndex) { this.robotsIndex = robotsIndex; }

    public int getSitemapMaxUrls() { return sitemapMaxUrls; }
    public void setSitemapMaxUrls(int sitemapMaxUrls) { this.sitemapMaxUrls = sitemapMaxUrls; }
}
