package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.ProtocolApi;
import com.iteaj.iboot.module.iot.service.IProtocolApiService;
import com.iteaj.framework.BaseController;

/**
 * 协议接口管理
 * @author iteaj
 * @since 2023-09-21
 */
@RestController
@RequestMapping("/iot/protocolApi")
public class ProtocolApiController extends BaseController {

    private final IProtocolApiService protocolApiService;

    public ProtocolApiController(IProtocolApiService protocolApiService) {
        this.protocolApiService = protocolApiService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览协议接口功能")
    @CheckPermission({"iot:protocolApi:view"})
    public Result<IPage<ProtocolApi>> list(Page<ProtocolApi> page, ProtocolApi entity) {
        return this.protocolApiService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:protocolApi:edit"})
    public Result<ProtocolApi> getById(Long id) {
        return this.protocolApiService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新协议接口记录")
    @CheckPermission(value = {"iot:protocolApi:edit", "iot:protocolApi:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ProtocolApi entity) {
        return this.protocolApiService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除协议接口记录")
    @CheckPermission({"iot:protocolApi:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.protocolApiService.removeByIds(idList);
    }
}

