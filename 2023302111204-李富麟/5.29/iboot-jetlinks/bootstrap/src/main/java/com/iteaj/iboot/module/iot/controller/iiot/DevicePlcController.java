package com.iteaj.iboot.module.iot.controller.iiot;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.dto.DeviceDto;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * plc设备管理
 *
 * @author iteaj
 * @since 2022-05-15
 */
@RestController
@RequestMapping("/iot/plc")
public class DevicePlcController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;
    private final static String[] plcCodes = {ProtocolCodes.PLC_SIEMENS_S7.getValue(), ProtocolCodes.PLC_OMRON_TCP.getValue()};

    public DevicePlcController(IDeviceService deviceService, IProductService productService) {
        this.deviceService = deviceService;
        this.productService = productService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @CheckPermission({"iot:plc:view"})
    public Result<IPage<DeviceDto>> list(Page<Device> page, DeviceDto entity) {
        entity.setProtocolCodes(plcCodes);
        return this.deviceService.pageOfDetail(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    public Result<Device> getEditDetail(Long id) {
        return this.deviceService.getById(id);
    }

    /**
    * 新增或者修改记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:plc:edit", "iot:plc:add"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody Device entity) {
        if(entity.getId() == null) {
            Product product = productService.getById(entity.getProductId())
                    .ifNotPresentThrow("产品不存在").getData();
            if(product.getDeviceType() == DeviceType.Gateway) {
                String deviceSn = product.getProtocolCode();
                synchronized (this) {
                    Device device = this.deviceService.getOne(Wrappers.<Device>lambdaQuery()
                            .eq(Device::getProductId, entity.getProductId())
                            .orderByDesc(Device::getDeviceSn).last("limit 1")).getData();
                    if (device != null) {
                        String[] number = device.getDeviceSn().split("_");
                        Integer idValue = Integer.valueOf(number[number.length - 1]);
                        deviceSn = String.format("gwd_%s_%d_%04d", deviceSn.toLowerCase(), entity.getProductId(), (idValue + 1));
                    } else {
                        deviceSn = "gwd_"+deviceSn.toLowerCase()+"_" + entity.getProductId() + "_0001";
                    }

                    entity.setDeviceSn(deviceSn);
                    return this.deviceService.saveOrUpdate(entity);
                }
            }
        }

        return this.deviceService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @CheckPermission({"iot:plc:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.deviceService.removeByIds(idList);
    }

    /**
     * 获取Dtu产品列表
     * @param deviceType 设备类型
     * @return
     */
    @GetMapping("/products")
    public Result<List<Product>> products(DeviceType deviceType) {
        return this.productService.listOfProtocolCodes(deviceType, plcCodes, null);
    }
}

