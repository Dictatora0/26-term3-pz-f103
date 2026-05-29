package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.ProtocolAttr;
import com.iteaj.iboot.module.iot.service.IProtocolAttrService;
import com.iteaj.framework.BaseController;

/**
 * 协议属性管理
 * @author iteaj
 * @since 2023-09-21
 */
@RestController
@RequestMapping("/iot/protocolAttr")
public class ProtocolAttrController extends BaseController {

    private final IProtocolAttrService protocolAttrService;

    public ProtocolAttrController(IProtocolAttrService protocolAttrService) {
        this.protocolAttrService = protocolAttrService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览协议属性功能")
    @CheckPermission({"iot:protocolAttr:view"})
    public Result<IPage<ProtocolAttr>> list(Page<ProtocolAttr> page, ProtocolAttr entity) {
        return this.protocolAttrService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:protocolAttr:edit"})
    public Result<ProtocolAttr> getById(Long id) {
        return this.protocolAttrService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新协议属性记录")
    @CheckPermission(value = {"iot:protocolAttr:edit", "iot:protocolAttr:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ProtocolAttr entity) {
        return this.protocolAttrService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除协议属性记录")
    @CheckPermission({"iot:protocolAttr:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.protocolAttrService.removeByIds(idList);
    }
}

