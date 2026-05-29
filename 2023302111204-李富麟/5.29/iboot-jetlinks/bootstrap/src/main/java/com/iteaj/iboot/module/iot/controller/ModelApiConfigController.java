package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.ModelApiConfig;
import com.iteaj.iboot.module.iot.service.IModelApiConfigService;
import com.iteaj.framework.BaseController;

/**
 * 物模型接口配置管理
 * @author iteaj
 * @since 2023-09-12
 */
@RestController
@RequestMapping("/iot/modelApiConfig")
public class ModelApiConfigController extends BaseController {

    private final IModelApiConfigService modelApiConfigService;

    public ModelApiConfigController(IModelApiConfigService modelApiConfigService) {
        this.modelApiConfigService = modelApiConfigService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览物模型接口配置功能")
    @CheckPermission({"iot:modelApiConfig:view"})
    public Result<IPage<ModelApiConfig>> list(Page<ModelApiConfig> page, ModelApiConfig entity) {
        return this.modelApiConfigService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:modelApiConfig:edit"})
    public Result<ModelApiConfig> getById(Long id) {
        return this.modelApiConfigService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新物模型接口配置记录")
    @CheckPermission(value = {"iot:modelApiConfig:edit", "iot:modelApiConfig:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ModelApiConfig entity) {
        return this.modelApiConfigService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除物模型接口配置记录")
    @CheckPermission({"iot:modelApiConfig:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.modelApiConfigService.removeByIds(idList);
    }
}

