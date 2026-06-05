/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.ams.controller;

import com.mawuya.ams.seo.SeoModel;
import com.mawuya.ams.seo.SeoProperties;
import com.mawuya.ams.seo.SeoUtils;
import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.dataobject.CategoryDO;
import com.mawuya.core.dataobject.ReviewDO;
import com.mawuya.core.dataobject.TagDO;
import com.mawuya.core.listener.MySessionContext;
import com.mawuya.core.service.ArticleService;
import com.mawuya.core.service.CategoryService;
import com.mawuya.core.service.ReviewService;
import com.mawuya.core.service.TagService;
import com.mawuya.core.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台 controller：首页 / 详情 / 归档 / 分类。
 *
 * <p>每个页面都会装配 {@link SeoModel}：
 * <ul>
 *   <li>差异化 TDK（首页站名、文章页标题、分类/标签页 N 篇文章）</li>
 *   <li>面包屑（{@link SeoModel#addBreadcrumb}）</li>
 *   <li>JSON-LD 结构化数据（Article / BreadcrumbList / CollectionPage）</li>
 * </ul>
 * </p>
 *
 * @author 钟启辉
 */
@Controller
public class IndexController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    /** 描述长度上限（中文 ~70 个字符 ≈ 150 拼音字节） */
    private static final int DESC_MAX = 155;
    /** 单文章详情页的相关阅读数 */
    private static final int RELATED_LIMIT = 5;

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ReviewService reviewService;
    private final SeoProperties seoProperties;

    @Autowired
    public IndexController(ArticleService articleService,
                           CategoryService categoryService,
                           TagService tagService,
                           ReviewService reviewService,
                           SeoProperties seoProperties) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.reviewService = reviewService;
        this.seoProperties = seoProperties;
    }

    /**
     * 博客首页（GET 与 POST 都支持，POST 是分页表单提交）
     */
    @RequestMapping(value = {"/", "index.html", "index", "index.jsp"})
    public String toHomePage(Model model, HttpServletRequest request) {
        Page<ArticleDO> page = articleService.listByPageFromRequest(request);
        page.setUrl("index");

        List<CategoryDO> categoryList = categoryService.list(new HashMap<>(4));
        model.addAttribute("page", page)
                .addAttribute("categoryList", categoryList);

        SeoModel seo = SeoModel.of(seoProperties.getSiteName(), seoProperties.getDefaultDescription())
                .setKeywords(seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        // 首页只有一个面包屑节点
        seo.addBreadcrumb("首页", null);
        model.addAttribute("seo", seo);
        return "fts/index";
    }

    @RequestMapping("favicon")
    public String favicon() {
        return null;
    }

    /**
     * 文章详情：含上一篇 / 下一篇 / 分类 / 标签 / 评论列表。
     *
     * <p>路径变量 {@code aid} 限定为纯数字（regex {@code \d+}），从根本上避免与
     * /sitemap.xml、/robots.txt、/rss.xml、/about、/categories、/tags、/archive、/search
     * 等顶层路由发生匹配冲突。</p>
     */
    @RequestMapping("{aid:\\d+}")
    public String getArticle(@PathVariable("aid") String aid, Model model, HttpServletRequest request) {
        Integer sn;
        try {
            sn = Integer.parseInt(aid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        MySessionContext.getInstance().addArticleSn2Session(request.getSession(), aid);

        ArticleDO info = articleService.getById(sn);
        if (info == null) {
            return ret404Page();
        }

        ArticleDO next = articleService.getNextById(sn);
        ArticleDO prev = articleService.getPrevById(sn);
        CategoryDO c = categoryService.getById(info.getCategorySn());
        List<TagDO> tags = tagService.listByArticleSn(sn);
        List<ReviewDO> reviews = reviewService.listByArticle(sn);
        List<ArticleDO> related = buildRelated(info, c, tags);

        model.addAttribute("article", info)
                .addAttribute("next", next)
                .addAttribute("prev", prev)
                .addAttribute("category", c)
                .addAttribute("tags", tags)
                .addAttribute("reviews", reviews)
                .addAttribute("related", related);

        model.addAttribute("seo", buildArticleSeo(info, c, tags));
        return "fts/show_article";
    }

    /**
     * 归档页面
     */
    @GetMapping("archive")
    public String toArchive(Model model) {
        Map<String, List<ArticleDO>> map = articleService.listGroupByYear();
        int count = articleService.count(new HashMap<>(4));
        model.addAttribute("map", map).addAttribute("count", count);

        SeoModel seo = SeoModel.of("文章归档",
                "按年份归档展示 " + count + " 篇文章，方便浏览历史内容。")
                .setKeywords("归档,文章列表,时间线," + seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/archive")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("归档", null);
        model.addAttribute("seo", seo);
        return "fts/archive";
    }

    /**
     * 博客分类列表
     */
    @GetMapping("categories")
    public String categoryList(Model model) {
        List<CategoryDO> categoryList = categoryService.listAllWithArtSize();
        model.addAttribute("categoryList", categoryList);

        SeoModel seo = SeoModel.of("分类导航",
                "共有 " + (categoryList == null ? 0 : categoryList.size()) + " 个分类，按主题快速找到你感兴趣的文章。")
                .setKeywords("分类,目录," + seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/categories")
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()));
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("分类", null);
        model.addAttribute("seo", seo);
        return "fts/category_list";
    }

    /**
     * 博客分类下的文章列表
     */
    @RequestMapping("categories/{cid:\\d+}")
    public String getCategory(@PathVariable("cid") String cid, Model model) {
        Integer sn;
        try {
            sn = Integer.parseInt(cid);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        CategoryDO category = categoryService.getDetailWithArtsBySn(sn);
        if (category == null) {
            return ret404Page();
        }
        model.addAttribute("category", category);

        int n = category.getArts() == null ? 0 : category.getArts().size();
        SeoModel seo = SeoModel.of(category.getCategoryName() + " 分类",
                "「" + category.getCategoryName() + "」分类下共 " + n + " 篇文章，覆盖该领域的实战经验与思考。")
                .setKeywords(category.getCategoryName() + "," + seoProperties.getDefaultKeywords())
                .setOgType("website")
                .setCanonical(seoProperties.getSiteUrl() + "/categories/" + sn)
                .setImage(SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage()))
                .addJsonLd(buildCollectionJsonLd(category, sn));
        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/")
                .addBreadcrumb("分类", seoProperties.getSiteUrl() + "/categories")
                .addBreadcrumb(category.getCategoryName(), null);
        model.addAttribute("seo", seo);
        return "fts/category";
    }

    // ============================== private helpers ==============================

    /**
     * 推荐"相关文章"：先按相同分类填充，不足再用任一标签命中的文章补齐，最后去重 / 去自身。
     * 这种内链结构对 SEO 价值很大：把权重从详情页传递给同主题的其他详情页。
     */
    private List<ArticleDO> buildRelated(ArticleDO current, CategoryDO c, List<TagDO> tags) {
        Map<Integer, ArticleDO> picked = new LinkedHashMap<>(RELATED_LIMIT * 2);
        if (c != null && c.getArts() != null) {
            for (ArticleDO a : c.getArts()) {
                if (a.getSn() != null && !a.getSn().equals(current.getSn())) {
                    picked.put(a.getSn(), a);
                    if (picked.size() >= RELATED_LIMIT) {
                        break;
                    }
                }
            }
        }
        if (picked.size() < RELATED_LIMIT && tags != null) {
            for (TagDO t : tags) {
                if (picked.size() >= RELATED_LIMIT) break;
                List<Integer> sns = tagService.listArticleSnByTag(t.getSn());
                List<ArticleDO> list = articleService.listBySnList(sns);
                if (list == null) continue;
                for (ArticleDO a : list) {
                    if (picked.size() >= RELATED_LIMIT) break;
                    if (a.getSn() == null || a.getSn().equals(current.getSn())) continue;
                    picked.putIfAbsent(a.getSn(), a);
                }
            }
        }
        return new ArrayList<>(picked.values());
    }

    /**
     * 构造文章页 SeoModel：含 article:* og 元字段、Article + BreadcrumbList JSON-LD。
     */
    private SeoModel buildArticleSeo(ArticleDO info, CategoryDO c, List<TagDO> tags) {
        String desc = SeoUtils.buildDescription(info.getArticleContent(), info.getArticleSummary(), DESC_MAX);
        if (desc.isEmpty()) {
            desc = info.getArticleTitle();
        }

        // 优先 picture_url（多张取第一张），其次 markdown 内首图，最后默认 OG 图
        String image = null;
        if (info.getPictureUrl() != null && !info.getPictureUrl().isEmpty()) {
            String first = info.getPictureUrl().split(";")[0];
            image = SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), first);
        }
        if (image == null) {
            String firstInBody = SeoUtils.firstImage(info.getArticleContent());
            if (firstInBody != null) {
                image = SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), firstInBody);
            }
        }
        if (image == null) {
            image = SeoUtils.toAbsoluteUrl(seoProperties.getSiteUrl(), seoProperties.getDefaultOgImage());
        }

        // 关键字：标题 + 分类 + 标签 + 站点默认
        StringBuilder kw = new StringBuilder(64);
        if (c != null) kw.append(c.getCategoryName()).append(',');
        if (tags != null) {
            for (TagDO t : tags) {
                kw.append(t.getTagName()).append(',');
            }
        }
        kw.append(seoProperties.getDefaultKeywords());

        List<String> tagNames = new ArrayList<>(tags == null ? 0 : tags.size());
        if (tags != null) {
            for (TagDO t : tags) {
                tagNames.add(t.getTagName());
            }
        }

        SeoModel seo = SeoModel.of(info.getArticleTitle(), desc)
                .setKeywords(kw.toString())
                .setOgType("article")
                .setCanonical(seoProperties.getSiteUrl() + "/" + info.getSn())
                .setImage(image)
                .setArticlePublishedTime(SeoUtils.toIso8601(info.getInsertTime()))
                .setArticleModifiedTime(SeoUtils.toIso8601(
                        info.getUpdateTime() != null ? info.getUpdateTime() : info.getInsertTime()))
                .setArticleAuthor(seoProperties.getAuthor())
                .setArticleSection(c == null ? null : c.getCategoryName())
                .setArticleTags(tagNames)
                .addJsonLd(buildArticleJsonLd(info, c, tagNames, desc, image));

        seo.addBreadcrumb("首页", seoProperties.getSiteUrl() + "/");
        if (c != null) {
            seo.addBreadcrumb(c.getCategoryName(), seoProperties.getSiteUrl() + "/categories/" + c.getSn());
        }
        seo.addBreadcrumb(info.getArticleTitle(), null);
        return seo;
    }

    /**
     * Article + BlogPosting 双类型 JSON-LD（Google 主流推荐）。
     */
    private String buildArticleJsonLd(ArticleDO a, CategoryDO c, List<String> tagNames,
                                      String desc, String image) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{");
        sb.append("\"@context\":\"https://schema.org\",");
        sb.append("\"@type\":\"BlogPosting\",");
        sb.append("\"mainEntityOfPage\":{\"@type\":\"WebPage\",\"@id\":\"")
                .append(seoProperties.getSiteUrl()).append("/").append(a.getSn()).append("\"},");
        sb.append("\"headline\":\"").append(SeoUtils.escapeJson(a.getArticleTitle())).append("\",");
        sb.append("\"description\":\"").append(SeoUtils.escapeJson(desc)).append("\",");
        if (image != null) {
            sb.append("\"image\":\"").append(SeoUtils.escapeJson(image)).append("\",");
        }
        sb.append("\"author\":{\"@type\":\"Person\",\"name\":\"")
                .append(SeoUtils.escapeJson(seoProperties.getAuthor())).append("\"},");
        sb.append("\"publisher\":{\"@type\":\"Organization\",\"name\":\"")
                .append(SeoUtils.escapeJson(seoProperties.getSiteName())).append("\"},");
        String published = SeoUtils.toIso8601(a.getInsertTime());
        String modified = SeoUtils.toIso8601(a.getUpdateTime() != null ? a.getUpdateTime() : a.getInsertTime());
        if (published != null) sb.append("\"datePublished\":\"").append(published).append("\",");
        if (modified != null) sb.append("\"dateModified\":\"").append(modified).append("\",");
        if (c != null) {
            sb.append("\"articleSection\":\"").append(SeoUtils.escapeJson(c.getCategoryName())).append("\",");
        }
        if (tagNames != null && !tagNames.isEmpty()) {
            sb.append("\"keywords\":\"");
            for (int i = 0; i < tagNames.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(SeoUtils.escapeJson(tagNames.get(i)));
            }
            sb.append("\",");
        }
        sb.append("\"inLanguage\":\"zh-CN\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 分类详情页：CollectionPage 结构化数据，描述本页是文章集合。
     */
    private String buildCollectionJsonLd(CategoryDO c, Integer sn) {
        return "{"
                + "\"@context\":\"https://schema.org\","
                + "\"@type\":\"CollectionPage\","
                + "\"name\":\"" + SeoUtils.escapeJson(c.getCategoryName()) + "\","
                + "\"url\":\"" + seoProperties.getSiteUrl() + "/categories/" + sn + "\","
                + "\"inLanguage\":\"zh-CN\""
                + "}";
    }
}
