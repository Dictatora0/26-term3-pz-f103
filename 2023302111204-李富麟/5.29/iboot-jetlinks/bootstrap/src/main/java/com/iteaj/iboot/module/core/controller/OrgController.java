package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.iboot.module.core.entity.Org;
import com.iteaj.iboot.module.core.service.IOrgService;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织部门管理
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/core/org")
public class OrgController extends BaseController {

    private final IOrgService organizeService;

    public OrgController(IOrgService organizeService) {
        this.organizeService = organizeService;
    }

    /**
     * 获取部门列表
     * @param org
     * @return
     */
    @Logger("查询机构列表")
    @GetMapping("/view")
    @CheckPermission("core:org:view")
    public Result<List<Org>> view(Org org) {
        return this.organizeService.selectTrees(org);
    }

    @ResponseBody
    @GetMapping("/parent")
    public Result<List<Org>> parent() {
        return this.organizeService.selectTrees(null);
    }

    /**
     * 新增部门
     * @param org
     * @return
     */
    @Logger("新增机构记录")
    @PostMapping("/add")
    @CheckPermission("core:org:add")
    public Result<Boolean> add(@RequestBody Org org) {
        return this.organizeService.save(org);
    }

    /**
     * 获取编辑详情
     * @param id
     * @return
     */
    @GetMapping("/edit")
    @CheckPermission("core:org:edit")
    public Result<Org> edit(Integer id) {
        return this.organizeService.getById(id);
    }

    /**
     * 修改部门
     * @param org
     * @return
     */
    @Logger("修改机构记录")
    @PostMapping("/edit")
    @CheckPermission("core:org:edit")
    public Result<Boolean> edit(@RequestBody Org org) {
        return this.organizeService.updateById(org);
    }

    /**
     * 删除部门
     * @param list
     * @return
     */
    @Logger("删除机构记录")
    @PostMapping("/del")
    @CheckPermission("core:org:del")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        return this.organizeService.removeByIds(list);
    }
}
