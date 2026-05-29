package com.iteaj.iboot.module.iot.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.entity.PointGroup;
import com.iteaj.iboot.module.iot.service.IPointGroupService;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;

/**
 * 点位组管理
 *
 * @author iteaj
 * @since 2022-07-22
 */
@RestController
@RequestMapping("/iot/pointGroup")
public class PointGroupController extends BaseController {

    private final IPointGroupService pointGroupService;

    public PointGroupController(IPointGroupService pointGroupService) {
        this.pointGroupService = pointGroupService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @CheckPermission({"iot:pointGroup:view"})
    public Result<IPage<PointGroup>> list(Page<PointGroup> page, PointGroup entity) {
        return this.pointGroupService.detailOfPage(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:pointGroup:edit"})
    public Result<PointGroup> getById(Long id) {
        return this.pointGroupService.detailById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:pointGroup:edit", "iot:pointGroup:add"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody PointGroup entity) {
        return this.pointGroupService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @CheckPermission({"iot:pointGroup:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.pointGroupService.removeByIds(idList);
    }

    /**
     * 获取点位组列表
     * @return
     */
    @GetMapping("list")
    public Result<List<PointGroup>> list() {
        return this.pointGroupService.list(Wrappers.emptyWrapper());
    }
}

