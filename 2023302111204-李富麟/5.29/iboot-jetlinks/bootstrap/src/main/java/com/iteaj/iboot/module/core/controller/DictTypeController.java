package com.iteaj.iboot.module.core.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.iboot.module.core.entity.DictType;
import com.iteaj.iboot.module.core.service.IDictTypeService;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  字典类型管理
 * @author iteaj
 */
@RestController
@RequestMapping("/core/dictType")
public class DictTypeController extends BaseController {

    private final IDictTypeService dictTypeService;

    public DictTypeController(IDictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    /**
     * 查询字典列表(分页)
     * @param dictType
     * @param page
     * @return
     */
    @Logger("查询字典列表")
    @GetMapping("/view")
    @CheckPermission("core:dictType:view")
    public Result<Page<DictType>> list(DictType dictType, Page page) {
        return dictTypeService.page(page.addOrder(OrderItem.desc("create_time")), dictType);
    }

    /**
     * 获取所有字典类型记录
     * @param dictType
     * @return
     */
    @GetMapping("/list")
    public Result<List<DictType>> list(DictType dictType) {
        return dictTypeService.list(dictType);
    }

    /**
     * 新增字典记录
     * @param dictType
     * @return
     */
    @Logger("新增字典")
    @PostMapping("/add")
    @CheckPermission("core:dictType:add")
    public Result<Boolean> add(@RequestBody DictType dictType) {
        return dictTypeService.save(dictType);
    }

    /**
     * 获取字典记录
     * @param id
     * @return
     */
    @GetMapping("/edit")
    @CheckPermission("core:dictType:edit")
    public Result<DictType> edit(Long id) {
        return dictTypeService.getById(id);
    }

    /**
     * 修改字典记录
     * @param dictType
     * @return
     */
    @Logger("修改字典记录")
    @PostMapping("/edit")
    @CheckPermission("core:dictType:edit")
    public Result<Boolean> edit(@RequestBody DictType dictType) {
        return dictTypeService.updateById(dictType);
    }

    /**
     * 删除字典
     * @param list
     * @return
     */
    @Logger("删除字典记录")
    @PostMapping("/del")
    @CheckPermission("core:dictType:del")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        return dictTypeService.removeByIds(list);
    }
}
