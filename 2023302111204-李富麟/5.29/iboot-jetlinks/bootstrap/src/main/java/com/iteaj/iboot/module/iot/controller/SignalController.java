package com.iteaj.iboot.module.iot.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.dto.SignalOfProductDto;
import com.iteaj.iboot.module.iot.entity.Signal;
import com.iteaj.iboot.module.iot.service.ISignalService;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;

/**
 * 寄存器点位管理
 *
 * @author iteaj
 * @since 2022-07-22
 */
@RestController
@RequestMapping("/iot/signal")
public class SignalController extends BaseController {

    private final ISignalService signalService;

    public SignalController(ISignalService signalService) {
        this.signalService = signalService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @CheckPermission({"iot:signal:view"})
    public Result<IPage<Signal>> list(Page<Signal> page, Signal entity) {
        return this.signalService.detailByPage(page, entity);
    }

    /**
     * 获取指定产品下面的点位列表
     * @param name 点位名
     * @param productIds 产品列表
     */
    @GetMapping("/listByProductIds")
    public Result<List<SignalOfProductDto>> listByProducts(Long[] productIds, String name) {
        List<Long> ids = productIds == null ? null : Arrays.asList(productIds);
        List<SignalOfProductDto> modelDtos = this.signalService.listByProductIds(ids, name)
                .stream().map(item -> new SignalOfProductDto().setId(item.getId().toString())
                .setName(item.getName()).setAddress(item.getAddress()).setProductName(item.getProductName())
                .setFieldName(item.getFieldName())).collect(Collectors.toList());
        return success(modelDtos);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:signal:edit"})
    public Result<Signal> getById(Long id) {
        return this.signalService.detailById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @Logger("保存点位信息")
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:signal:edit", "iot:signal:add"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody Signal entity) {
        return this.signalService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @Logger("删除点位信息")
    @PostMapping("/del")
    @CheckPermission({"iot:signal:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.signalService.removeByIds(idList);
    }
}

