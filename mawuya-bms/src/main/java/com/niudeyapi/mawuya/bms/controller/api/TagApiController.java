/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller.api;

import com.niudeyapi.mawuya.bms.dto.request.ArticleTagBindRequest;
import com.niudeyapi.mawuya.bms.dto.request.SnRequest;
import com.niudeyapi.mawuya.bms.dto.request.TagCreateRequest;
import com.niudeyapi.mawuya.bms.dto.request.TagUpdateRequest;
import com.niudeyapi.mawuya.core.common.BaseResponse;
import com.niudeyapi.mawuya.core.dataobject.TagDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 标签管理 JSON API。
 *
 * <ul>
 *   <li>{@code GET  /bms/api/tag/listAll}     列出所有标签（含文章计数，给 select2 / 列表用）</li>
 *   <li>{@code POST /bms/api/tag/create}      新建标签</li>
 *   <li>{@code POST /bms/api/tag/update}      更新标签</li>
 *   <li>{@code POST /bms/api/tag/delete}      删除标签（级联清 article_tag 关联）</li>
 *   <li>{@code POST /bms/api/tag/bindArticle} 为文章重绑标签（支持同时传入已有 sn + 新建标签名）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/tag")
public class TagApiController {

    @Autowired
    private TagService tagService;

    @GetMapping("listAll")
    public BaseResponse<List<TagDO>> listAll() {
        // 复用标签云查询，带 artSize 字段，便于前端展示「N 篇文章」
        return BaseResponse.success(tagService.listTagCloud());
    }

    @PostMapping("create")
    public BaseResponse<Void> create(@Valid TagCreateRequest req) {
        if (tagService.save(req.getTagName(), req.getTagColor()) <= 0) {
            throw new BusinessException("创建失败");
        }
        return BaseResponse.success("创建成功", null);
    }

    @PostMapping("update")
    public BaseResponse<Void> update(@Valid TagUpdateRequest req) {
        if (tagService.updateByIdSafely(req.getSn(), req.getTagName(), req.getTagColor()) <= 0) {
            throw new BusinessException("更新失败");
        }
        return BaseResponse.success("更新成功", null);
    }

    @PostMapping("delete")
    public BaseResponse<Void> delete(@Valid SnRequest req) {
        if (tagService.removeWithRelations(req.getSn()) <= 0) {
            throw new BusinessException("删除失败");
        }
        return BaseResponse.success("已删除", null);
    }

    /**
     * 为文章重绑标签。<strong>会先解绑该文章已有的所有标签，再按入参重新绑定</strong>。
     * 传空数组等价于「清空该文章所有标签」。
     */
    @PostMapping("bindArticle")
    public BaseResponse<Integer> bindArticle(@Valid ArticleTagBindRequest req) {
        int rows = tagService.rebindArticleTagsByMixed(
                req.getArticleSn(), req.getExistingTagSns(), req.getNewTagNames());
        return BaseResponse.success("已更新文章标签", rows);
    }
}
