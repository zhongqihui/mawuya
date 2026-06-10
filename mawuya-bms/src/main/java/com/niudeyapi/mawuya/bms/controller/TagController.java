/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller;

import com.niudeyapi.mawuya.bms.controller.api.TagApiController;
import com.niudeyapi.mawuya.core.dataobject.TagDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 后台标签管理视图 controller。
 *
 * <p>JSON 操作（增/删/改/绑定）已迁移至 {@link TagApiController}。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/tag")
public class TagController extends BaseController {

    @Autowired
    private TagService tagService;

    /** 标签列表页：附带每个标签的文章计数（artSize），便于"删除前确认"。 */
    @GetMapping("list.do")
    public String tagList(Model model) {
        List<TagDO> tagList = tagService.listTagCloud();
        model.addAttribute("tagList", tagList);
        return "bms/tag/tag_list";
    }

    /** 新增标签弹层（layer.open type=1 加载此页面） */
    @GetMapping("toAdd.do")
    public String toAdd() {
        return "bms/tag/mod_tag";
    }

    /** 编辑标签弹层 */
    @GetMapping("toUpdate.do")
    public String toUpdate(@RequestParam("sn") String sn, Model model) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return ret404Page();
        }

        TagDO tag = tagService.getById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        model.addAttribute("tag", tag);
        return "bms/tag/mod_tag";
    }
}
