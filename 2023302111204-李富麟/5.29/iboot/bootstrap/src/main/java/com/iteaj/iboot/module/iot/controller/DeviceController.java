package com.iteaj.iboot.module.iot.controller;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.DeviceDto;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iboot.module.iot.utils.IotNetworkUtil;
import io.netty.channel.ChannelFuture;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 设备管理
 *
 * @author iteaj
 * @since 2022-05-15
 */
@RestController
@RequestMapping("/iot/device")
public class DeviceController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;

    public DeviceController(IDeviceService deviceService, IProductService productService) {
        this.deviceService = deviceService;
        this.productService = productService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @Logger("浏览设备记录")
    @GetMapping("/view")
    @CheckPermission({"iot:device:view"})
    public Result<IPage<DeviceDto>> list(Page<Device> page, DeviceDto entity) {
        return this.deviceService.pageOfDetail(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:device:edit"})
    public Result<DeviceDto> getEditDetail(Long id) {
        return this.deviceService.detailById(id);
    }

    /**
    * 修改记录
    * @param entity
    */
    @Logger("修改设备记录")
    @PostMapping("/edit")
    @CheckPermission({"iot:device:edit"})
    public Result<Boolean> edit(@RequestBody Device entity) {
        return this.deviceService.updateById(entity);
    }

    /**
    * 新增记录
    * @param entity
    */
    @Logger("新增设备记录")
    @PostMapping("/add")
    @CheckPermission({"iot:device:add"})
    public Result<Boolean> add(@RequestBody Device entity) {
        return this.deviceService.save(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @Logger("删除设备记录")
    @PostMapping("/del")
    @CheckPermission({"iot:device:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.deviceService.removeByIds(idList);
    }

    /**
     * 获取指定型号下面的设备列表
     * @param modelId
     * @return
     */
    @GetMapping("listByModel")
    public Result<List<Device>> listByModel(Long modelId) {
        return this.deviceService.list(Wrappers.<Device>lambdaQuery().eq(Device::getProductId, modelId));
    }

    /**
     * 获取网关设备
     * @param gatewayId 网关id
     * @return
     */
    @GetMapping("listOfGateway")
    public Result<List<Device>> listOfGateway(Long gatewayId) {
        return this.deviceService.listOfGateway(gatewayId);
    }

    /**
     * 获取指定产品列表下的所有设备
     * @param productIds
     * @return
     */
    @GetMapping("listOfProducts")
    public Result<List<Device>> listOfProducts(Long[] productIds) {
        if(ArrayUtil.isEmpty(productIds)) {
            return success(Collections.EMPTY_LIST);
        }

        return this.deviceService.list(Wrappers.<Device>lambdaQuery().in(Device::getProductId, productIds));
    }

    /**
     * 设备连接
     * @param device
     * @return
     */
    @PostMapping("connect/{status}")
    @CheckPermission(value = {"iot:modbus:connect", "iot:mqtt:connect", "iot:device:gateway:connect"}, logical = Logical.OR)
    public Result connect(@RequestBody Device device, @PathVariable DeviceStatus status) {
        if(status == null) {
            return fail("未指定连接状态");
        }

        DeviceDto entity = deviceService.detailById(device.getId())
                .ifNotPresentThrow("设备不存在["+device.getDeviceSn()+"]")
                .getData();

        this.productService.getById(entity.getProductId()).ifNotPresentThrow("产品不存在")
                .ifPresent(product -> {
                   if(product.getStatus() == FuncStatus.disabled) {
                       throw new ServiceException("请先启用产品["+product.getName()+"]");
                   }
                });

        try {
            FuncStatus funcStatus = status == DeviceStatus.online ? FuncStatus.enabled : FuncStatus.disabled;
            Object networkCtrl = IotNetworkUtil.networkCtrl(entity.getProtocolCode(), entity, funcStatus);
            if(networkCtrl instanceof ChannelFuture) {
                ((ChannelFuture) networkCtrl).get(10, TimeUnit.SECONDS);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return fail("连接失败");
        }

        return success("切换状态成功");
    }
}

