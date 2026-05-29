package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.ProtocolApiConfig;
import com.iteaj.iboot.module.iot.service.IProtocolApiConfigService;
import com.iteaj.framework.BaseController;

/**
 * 协议接口配置管理
 * @author iteaj
 * @since 2023-09-21
 */
@RestController
@RequestMapping("/iot/protocolApiConfig")
public class ProtocolApiConfigController extends BaseController {

    private final IProtocolApiConfigService protocolApiConfigService;

    public ProtocolApiConfigController(IProtocolApiConfigService protocolApiConfigService) {
        this.protocolApiConfigService = protocolApiConfigService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览协议接口配置功能")
    @CheckPermission({"iot:protocolApiConfig:view"})
    public Result<IPage<ProtocolApiConfig>> list(Page<ProtocolApiConfig> page, ProtocolApiConfig entity) {
        return this.protocolApiConfigService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:protocolApiConfig:edit"})
    public Result<ProtocolApiConfig> getById(Long id) {
        return this.protocolApiConfigService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新协议接口配置记录")
    @CheckPermission(value = {"iot:protocolApiConfig:edit", "iot:protocolApiConfig:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ProtocolApiConfig entity) {
        return this.protocolApiConfigService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除协议接口配置记录")
    @CheckPermission({"iot:protocolApiConfig:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.protocolApiConfigService.removeByIds(idList);
    }
}

