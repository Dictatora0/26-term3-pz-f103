package com.iteaj.iboot.module.iot.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.service.IModelApiService;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.service.IDeviceGroupService;
import com.iteaj.framework.BaseController;

/**
 * 设备分组管理
 * @author iteaj
 */
@RestController
@RequestMapping("/iot/deviceGroup")
public class DeviceGroupController extends BaseController {

    private final IModelApiService modelApiService;
    private final IDeviceGroupService deviceGroupService;

    public DeviceGroupController(IModelApiService modelApiService
            , IDeviceGroupService deviceGroupService) {
        this.modelApiService = modelApiService;
        this.deviceGroupService = deviceGroupService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览设备分组功能")
    @CheckPermission({"iot:deviceGroup:view"})
    public Result<IPage<DeviceGroup>> list(Page<DeviceGroup> page, DeviceGroup entity) {
        return this.deviceGroupService.page(page, entity);
    }

    /**
     * 获取所有组列表
     * @return
     */
    @GetMapping("/list")
    public Result<List<DeviceGroup>> groups() {
        return this.deviceGroupService.list();
    }

    /**
     * 树结构分组列表
     * @param entity
     * @return
     */
    @GetMapping("/tree")
    public Result<List<DeviceGroup>> tree(DeviceGroup entity) {
        return this.deviceGroupService.tree(entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:deviceGroup:edit"})
    public Result<DeviceGroup> getById(Long id) {
        return this.deviceGroupService.detailById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新设备分组记录")
    @CheckPermission(value = {"iot:deviceGroup:edit", "iot:deviceGroup:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody DeviceGroup entity) {
        return this.deviceGroupService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除设备分组记录")
    @CheckPermission({"iot:deviceGroup:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.deviceGroupService.removeByIds(idList);
    }

    /**
     * 获取物模型接口列表
     * @param deviceGroupId 设备组id
     * @return
     */
    @GetMapping("listModelApi")
    public Result<List<IVOption>> listModelApi(Long deviceGroupId) {
        if(deviceGroupId == null) {
            return success(Collections.EMPTY_LIST);
        }

        DeviceGroup data = this.deviceGroupService.getGroupProductById(deviceGroupId)
                .ifNotPresentThrow("设备组不存在").getData();
        if(data.getProductIds() != null && !data.getProductIds().isEmpty()) {
            List<Long> longs = CollectionUtil.newArrayList(data.getProductIds().iterator())
                    .stream().map(item -> Long.valueOf(item.toString())).collect(Collectors.toList());
            List<ModelApi> list = modelApiService.list(Wrappers.<ModelApi>lambdaQuery()
                    .in(ModelApi::getProductId, longs).eq(ModelApi::getType, ProtocolApiType.event))
                    .ofNullable().orElse(Collections.EMPTY_LIST);

            List<IVOption> ivOptions = list.stream()
                    .map(item -> new IVOption(item.getName(), item.getId().toString(), item.getCode())
                            .addConfig("trigger", item.getTriggerMode()).addConfig("productId", item.getProductId()))
                    .collect(Collectors.toList());
            return success(ivOptions);
        } else {
            return success(Collections.EMPTY_LIST);
        }
    }
}

