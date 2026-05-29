/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.seed;

import com.qihuizhong.mawuya.core.dataobject.ArticleDO;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import com.qihuizhong.mawuya.core.service.ImageBlobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把现有所有"在线图片 / 本地文件图片"迁移到 image_blob 表。
 *
 * <p>触发方式（避免误触发）：</p>
 * <pre>
 * mvn -pl mawuya-bms -Dtest=ImageMigrationLoader -DMAWUYA_MIGRATE=run test
 * </pre>
 *
 * <p>处理范围：</p>
 * <ol>
 *   <li>article_info.picture_url —— 把每条 url（分号分隔）下载/读取后入库，整体替换为 /image/db/{sn}（仍用分号分隔）</li>
 *   <li>article_content（Markdown 正文）—— 提取 ![](src) 与 &lt;img src=""&gt;，对外链/绝对路径图片入库后替换</li>
 * </ol>
 *
 * @author 钟启辉
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "MAWUYA_MIGRATE", matches = "run")
public class ImageMigrationLoader {

    @Autowired
    private ArticleInfoMapper articleInfoMapper;
    @Autowired
    private ImageBlobService imageBlobService;

    /** Markdown 图片：![alt](url)  与 <img src="..." > */
    private static final Pattern MD_IMG = Pattern.compile("!\\[[^\\]]*]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern HTML_IMG = Pattern.compile("<img[^>]*\\bsrc\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);

    /** 上传目录：与 application.yml 中 mawuya.upload.dir 保持一致 */
    private static final String UPLOAD_DIR = "/Users/zhongqihui/upload";
    /** 静态资源映射根：/statics → ams 模块的 static/statics */
    private static final String STATICS_FS_BASE = "/Users/zhongqihui/go/src/git.code.oa.com/gamecloud/mawuya/mawuya-ams/src/main/resources/static/statics/";

    /** 已迁移过的 url -> 新 sn，避免重复下载（同一进程内） */
    private final Map<String, Long> sessionCache = new HashMap<>();

    @Test
    public void migrate() {
        Map<String, String> empty = new HashMap<>();
        List<ArticleDO> all = articleInfoMapper.selectList(empty);
        System.out.println("[migrate] articles total = " + all.size());

        int picUrlChanged = 0;
        int contentChanged = 0;

        for (ArticleDO a : all) {
            // 1) picture_url（用分号分隔多张）
            String oldPic = a.getPictureUrl();
            String newPic = rewriteSemicolonList(oldPic);
            boolean picChanged = newPic != null && !Objects.equals(newPic, oldPic);

            // 2) article_content
            ArticleDO full = articleInfoMapper.selectById(a.getSn());
            String oldContent = full == null ? null : full.getArticleContent();
            String newContent = rewriteMarkdown(oldContent);
            boolean conChanged = newContent != null && !Objects.equals(newContent, oldContent);

            if (picChanged || conChanged) {
                if (conChanged && full != null) {
                    // update 是全字段覆盖，必须把读数/点赞数等也带上，避免被清零
                    ArticleDO upd = new ArticleDO()
                            .setSn(full.getSn())
                            .setCategorySn(full.getCategorySn())
                            .setReadNum(full.getReadNum())
                            .setReviewNum(full.getReviewNum())
                            .setPraiseNum(full.getPraiseNum())
                            .setTeaseNum(full.getTeaseNum())
                            .setPictureUrl(picChanged ? newPic : full.getPictureUrl())
                            .setArticleTitle(full.getArticleTitle())
                            .setArticleSummary(full.getArticleSummary())
                            .setArticleContent(newContent)
                            .setInsertTime(full.getInsertTime());
                    articleInfoMapper.update(upd);
                } else if (picChanged) {
                    // 只动了图片字段，走轻量更新
                    ArticleDO upd = new ArticleDO().setSn(a.getSn()).setPictureUrl(newPic);
                    articleInfoMapper.updatePictureUrl(upd);
                }
                if (picChanged) picUrlChanged++;
                if (conChanged) contentChanged++;
                System.out.println("[migrate] sn=" + a.getSn()
                        + " picChanged=" + picChanged
                        + " contentChanged=" + conChanged);
            }
        }
        System.out.println("[migrate] DONE. picture_url updated = " + picUrlChanged
                + ", article_content updated = " + contentChanged);
    }

    /** 处理分号分隔的 url 列表（picture_url 字段格式） */
    private String rewriteSemicolonList(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String[] parts = raw.split(";");
        StringBuilder sb = new StringBuilder();
        boolean changed = false;
        for (int i = 0; i < parts.length; i++) {
            String u = parts[i].trim();
            if (u.isEmpty()) continue;
            String r = rewriteOne(u);
            if (!u.equals(r)) changed = true;
            if (sb.length() > 0) sb.append(';');
            sb.append(r);
        }
        return changed ? sb.toString() : raw;
    }

    /** 处理 Markdown 正文：![]() 与 <img src=""> 都转成 /image/db/{sn} */
    private String rewriteMarkdown(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        StringBuilder out = new StringBuilder(raw.length() + 64);
        Matcher m = MD_IMG.matcher(raw);
        int last = 0;
        boolean changed = false;
        while (m.find()) {
            out.append(raw, last, m.start());
            String url = m.group(1);
            String newUrl = rewriteOne(url);
            if (!newUrl.equals(url)) changed = true;
            // 重新拼回 ![alt](newUrl)
            String full = m.group();
            out.append(full.replace(url, newUrl));
            last = m.end();
        }
        out.append(raw, last, raw.length());

        // 再处理 <img src="">
        String afterMd = out.toString();
        Matcher m2 = HTML_IMG.matcher(afterMd);
        StringBuilder out2 = new StringBuilder(afterMd.length() + 64);
        int last2 = 0;
        while (m2.find()) {
            out2.append(afterMd, last2, m2.start());
            String url = m2.group(1);
            String newUrl = rewriteOne(url);
            if (!newUrl.equals(url)) changed = true;
            out2.append(m2.group().replace(url, newUrl));
            last2 = m2.end();
        }
        out2.append(afterMd, last2, afterMd.length());
        return changed ? out2.toString() : raw;
    }

    /**
     * 单个 url → 新 url（/image/db/{sn}）。已经是新格式或无法处理则原样返回。
     */
    private String rewriteOne(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("/image/db/")) return url;          // 已迁移
        if (url.startsWith("data:")) return url;                // base64 inline，不动
        if (sessionCache.containsKey(url)) {
            return "/image/db/" + sessionCache.get(url);
        }
        // 1) 库内已有同 sourceUrl 记录？复用
        Long existSn = imageBlobService.getSnBySourceUrl(url);
        if (existSn != null) {
            sessionCache.put(url, existSn);
            return "/image/db/" + existSn;
        }

        try {
            byte[] bytes;
            String fileName;
            String contentType;

            if (url.startsWith("http://") || url.startsWith("https://")) {
                LoadedBytes lb = downloadHttp(url);
                if (lb == null) return url;
                bytes = lb.bytes;
                fileName = guessName(url);
                contentType = lb.contentType != null ? lb.contentType : guessContentType(fileName);
            } else if (url.startsWith("/upload/")) {
                Path p = Paths.get(UPLOAD_DIR, url.substring("/upload/".length()));
                if (!Files.exists(p)) {
                    System.out.println("[migrate] upload file missing: " + p);
                    return url;
                }
                bytes = Files.readAllBytes(p);
                fileName = p.getFileName().toString();
                contentType = guessContentType(fileName);
            } else if (url.startsWith("/statics/")) {
                Path p = Paths.get(STATICS_FS_BASE, url.substring("/statics/".length()));
                if (!Files.exists(p)) {
                    System.out.println("[migrate] statics file missing: " + p);
                    return url;
                }
                bytes = Files.readAllBytes(p);
                fileName = p.getFileName().toString();
                contentType = guessContentType(fileName);
            } else {
                // 其他相对路径暂不处理
                return url;
            }

            long sn = imageBlobService.save(bytes, fileName, contentType, url);
            sessionCache.put(url, sn);
            System.out.println("[migrate]  ✓ " + url + "  ->  /image/db/" + sn + "  (" + bytes.length + " bytes)");
            return "/image/db/" + sn;
        } catch (Exception e) {
            System.out.println("[migrate]  ✗ " + url + "  -> " + e.getMessage());
            return url;
        }
    }

    private static class LoadedBytes {
        byte[] bytes;
        String contentType;
    }

    private LoadedBytes downloadHttp(String url) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                                + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36");
                conn.setInstanceFollowRedirects(true);
                int code = conn.getResponseCode();
                if (code != 200) {
                    System.out.println("[migrate]   http " + code + " for " + url);
                    return null;
                }
                String ct = conn.getContentType();
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    LoadedBytes lb = new LoadedBytes();
                    lb.bytes = out.toByteArray();
                    lb.contentType = ct;
                    return lb;
                }
            } catch (IOException e) {
                System.out.println("[migrate]   download attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == 2) return null;
                try { Thread.sleep(800); } catch (InterruptedException ignored) { }
            }
        }
        return null;
    }

    private static String guessName(String url) {
        String tail = url;
        int q = tail.indexOf('?');
        if (q >= 0) tail = tail.substring(0, q);
        int slash = tail.lastIndexOf('/');
        if (slash >= 0) tail = tail.substring(slash + 1);
        if (tail.isEmpty() || !tail.contains(".")) {
            tail = "remote-" + System.currentTimeMillis() + ".jpg";
        }
        return tail.length() > 120 ? tail.substring(tail.length() - 120) : tail;
    }

    private static String guessContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "application/octet-stream";
        switch (fileName.substring(dot + 1).toLowerCase()) {
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png":  return "image/png";
            case "gif":  return "image/gif";
            case "bmp":  return "image/bmp";
            case "webp": return "image/webp";
            default:     return "application/octet-stream";
        }
    }
}
