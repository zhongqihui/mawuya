/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller;

import com.niudeyapi.mawuya.bms.controller.api.ArticleApiController;
import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.dataobject.ArticleStatusLogDO;
import com.niudeyapi.mawuya.core.dataobject.CategoryDO;
import com.niudeyapi.mawuya.core.dataobject.TagDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.service.ArticleService;
import com.niudeyapi.mawuya.core.service.CategoryService;
import com.niudeyapi.mawuya.core.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台文章管理视图 controller。
 *
 * <p>文章 CRUD / 上传等 JSON 操作已迁移至
 * {@link ArticleApiController}。</p>
 *
 * @author zqh
 */
@Controller
@RequestMapping("bms/article")
public class ArticleController extends BaseController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;

    /** 写文章页 */
    @GetMapping("toAdd.do")
    public String toWriteArticle(Model model) {
        List<CategoryDO> categories = categoryService.list(new HashMap<>(4));
        // 全部标签清单，给 select2 multiple 渲染下拉项；新建文章未关联标签，所以 selectedTagSns 为空
        List<TagDO> tagList = tagService.listTagCloud();
        model.addAttribute("categoryList", categories)
                .addAttribute("tagList", tagList)
                .addAttribute("selectedTagSns", new ArrayList<Integer>(0));
        return "bms/article/write_article";
    }

    /**
     * 文章列表页。
     *
     * <p>新增 {@code status} 查询参数，对应 tab 切换：
     * <ul>
     *   <li>不传 / "all" / 非法值 → 显示全部文章</li>
     *   <li>{@code 0} → 仅草稿</li>
     *   <li>{@code 1} → 仅已发布</li>
     *   <li>{@code 2} → 仅已撤回</li>
     * </ul></p>
     */
    @GetMapping("list.do")
    public String articleList(@RequestParam(value = "status", required = false) String status,
                              Model model) {
        Integer statusVal = parseStatus(status);
        List<ArticleDO> articleList = articleService.listAllNoContentByStatus(statusVal);
        List<CategoryDO> categoryList = categoryService.list(new HashMap<>(4));

        // 为每篇文章预取标签列表，列表页展示「标签徽章」列，与 AMS 端右侧栏数据来源一致。
        // 这里用 N+1 简单循环；BMS 文章数量级在百级，可接受；若未来需要优化可改为 JOIN 一次取回。
        Map<Integer, List<TagDO>> articleTagMap = new HashMap<>(articleList == null ? 0 : articleList.size() * 2);
        if (articleList != null) {
            for (ArticleDO a : articleList) {
                if (a.getSn() == null) continue;
                articleTagMap.put(a.getSn(), tagService.listByArticleSn(a.getSn()));
            }
        }

        model.addAttribute("articleList", articleList)
                .addAttribute("categoryList", categoryList)
                .addAttribute("articleTagMap", articleTagMap)
                // status 回显，前端 tab 高亮用
                .addAttribute("currentStatus", statusVal == null ? "all" : String.valueOf(statusVal));
        return "bms/article/article_list";
    }

    /** 编辑文章页 */
    @GetMapping("toUpdate.do")
    public String toUpdate(@RequestParam("sn") String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        ArticleDO articleInfo = articleService.getById(id);
        if (articleInfo == null) {
            throw new BusinessException("文章不存在");
        }
        List<CategoryDO> categories = categoryService.list(new HashMap<>(4));
        // 文章状态流转时间线：用于编辑页底部展示「何时草稿 / 何时发布 / 何时撤回」
        List<ArticleStatusLogDO> statusLogs = articleService.listStatusLogs(id);
        // 全量标签 + 当前文章已绑定标签 sn，前端用于初始化 select2 multiple 已选项
        List<TagDO> tagList = tagService.listTagCloud();
        List<Integer> selectedTagSns = tagService.listTagSnByArticle(id);
        Set<Integer> selectedTagSnSet = new HashSet<>(selectedTagSns);
        model.addAttribute("categoryList", categories)
                .addAttribute("article", articleInfo)
                .addAttribute("statusLogs", statusLogs)
                .addAttribute("tagList", tagList)
                .addAttribute("selectedTagSns", selectedTagSns)
                .addAttribute("selectedTagSnSet", selectedTagSnSet);
        return "bms/article/mod_article";
    }

    /**
     * 把字符串 status 解析为 Integer（0/1/2），"all" 或解析失败返回 null。
     *
     * <p>注意：取值受白名单约束，避免被注入到下游 Map 然后污染 mapper 参数；
     * 与 SQL 参数化绑定相结合，无注入空间。</p>
     */
    private Integer parseStatus(String raw) {
        if (raw == null || raw.isEmpty() || "all".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            int v = Integer.parseInt(raw);
            if (v == ArticleService.STATUS_DRAFT
                    || v == ArticleService.STATUS_PUBLISHED
                    || v == ArticleService.STATUS_WITHDRAWN) {
                return v;
            }
        } catch (NumberFormatException ignored) {
            // 非法值按"全部"处理
        }
        return null;
    }
}
