/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.seo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SEO 文本处理工具：从 Markdown / HTML 中提取摘要、首图、关键字，构造 ISO8601，
 * 以及给 JSON-LD 输出做安全转义。
 *
 * <p>所有方法均为纯函数，无 Spring 依赖，单元测试无需启动容器。</p>
 *
 * @author 钟启辉
 */
public final class SeoUtils {

    /** 提取 markdown / html 中的图片：![alt](url)、html <img src="..."> 通吃 */
    private static final Pattern MD_IMG = Pattern.compile("!\\[[^\\]]*]\\(([^)\\s]+)");
    private static final Pattern HTML_IMG = Pattern.compile("<img[^>]+src\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    /** 通用 HTML / markdown 标记剥离：尽量留可读文字 */
    private static final Pattern STRIP_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]+)]\\([^)]*\\)");
    private static final Pattern MD_HEADER = Pattern.compile("(?m)^#{1,6}\\s+");
    private static final Pattern MD_BOLD = Pattern.compile("\\*\\*([^*]+)\\*\\*");
    private static final Pattern MD_ITALIC = Pattern.compile("\\*([^*]+)\\*");
    private static final Pattern MD_CODE_FENCE = Pattern.compile("(?s)```[^`]*```");
    private static final Pattern MD_INLINE_CODE = Pattern.compile("`([^`]+)`");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private SeoUtils() {}

    /**
     * 从原始 markdown / 摘要里抽取干净的描述文本，截断至 maxLen（不超过则原样返回）。
     * 先做 markdown 标记剥离，再合并空白；适合 description / og:description。
     */
    public static String buildDescription(String raw, String summary, int maxLen) {
        String src = (summary != null && !summary.isEmpty()) ? summary : raw;
        if (src == null || src.isEmpty()) {
            return "";
        }
        String s = src;
        s = MD_CODE_FENCE.matcher(s).replaceAll(" ");
        s = MD_INLINE_CODE.matcher(s).replaceAll("$1");
        s = MD_HEADER.matcher(s).replaceAll("");
        s = MD_BOLD.matcher(s).replaceAll("$1");
        s = MD_ITALIC.matcher(s).replaceAll("$1");
        s = MD_LINK.matcher(s).replaceAll("$1");
        s = STRIP_TAG.matcher(s).replaceAll(" ");
        s = WHITESPACE.matcher(s).replaceAll(" ").trim();
        if (s.length() > maxLen) {
            // 在 maxLen 附近找一个标点，避免半字截断
            int cut = maxLen;
            int safe = Math.max(maxLen - 16, 0);
            for (int i = maxLen; i > safe; i--) {
                char c = s.charAt(i - 1);
                if (c == '。' || c == '!' || c == '?' || c == '；' || c == ';' || c == '.' || c == ',' || c == '，') {
                    cut = i;
                    break;
                }
            }
            s = s.substring(0, cut) + "…";
        }
        return s;
    }

    /**
     * 从 markdown / html 中提取首张图片 URL，找不到返回 null。
     * 注意：返回的可能是相对路径，调用方需用 {@link #toAbsoluteUrl} 升级为绝对地址。
     */
    public static String firstImage(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        Matcher m = MD_IMG.matcher(content);
        if (m.find()) {
            return m.group(1);
        }
        Matcher h = HTML_IMG.matcher(content);
        if (h.find()) {
            return h.group(1);
        }
        return null;
    }

    /**
     * 把可能是相对路径的资源 URL 升级为绝对地址。
     * - 已经以 http / https / // 开头：原样返回
     * - 否则补 siteUrl（不带尾斜杠）+ path（保证以 / 开头）
     */
    public static String toAbsoluteUrl(String siteUrl, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//")) {
            return path;
        }
        String base = siteUrl == null ? "" : siteUrl;
        String p = path.startsWith("/") ? path : "/" + path;
        return base + p;
    }

    /**
     * 构造 ISO8601 时间字符串（"yyyy-MM-ddTHH:mm:ss+08:00"）；
     * 入参允许 "yyyy-MM-dd HH:mm:ss" / "yyyy-MM-dd" / null。
     */
    public static String toIso8601(String dbTime) {
        if (dbTime == null || dbTime.isEmpty()) {
            return null;
        }
        String s = dbTime.trim();
        // 已经带 T 的认为本身合法
        if (s.contains("T")) {
            return s;
        }
        if (s.length() == 10) {
            return s + "T00:00:00+08:00";
        }
        if (s.length() >= 19) {
            return s.substring(0, 10) + "T" + s.substring(11, 19) + "+08:00";
        }
        return s;
    }

    /**
     * 给 HTML 属性输出做最小转义（&、<、>、"、'）。
     * 仅用于 meta content / link href 等"不会再被解析为 HTML 标签"的场景。
     */
    public static String escapeAttr(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 给 JSON-LD 文本字段做转义：双引号 / 反斜杠 / 控制字符全部转码。
     * 不依赖 Jackson —— 调用方多在拼模板时使用，单值轻量更直观。
     */
    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
