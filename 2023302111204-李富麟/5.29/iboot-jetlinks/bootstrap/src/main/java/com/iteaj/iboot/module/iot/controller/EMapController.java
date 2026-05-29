package com.iteaj.iboot.module.iot.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.consts.AttrType;
import com.iteaj.framework.spi.iot.consts.DataType;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.iboot.module.iot.cache.data.RealtimeData;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.dto.EMapDetailDto;
import com.iteaj.iboot.module.iot.dto.EMapDeviceDto;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelApiConfig;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 电子地图模块
 */
@RestController
@RequestMapping("/iot/emap")
public class EMapController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;
    private final RealtimeDataService realtimeDataService;

    public EMapController(IDeviceService deviceService, IProductService productService
            , RealtimeDataService realtimeDataService) {
        this.deviceService = deviceService;
        this.productService = productService;
        this.realtimeDataService = realtimeDataService;
    }

    /**
     * 获取设备列表
     * @param productId 产品id
     * @param deviceGroupId 设备组id
     * @return
     */
    @GetMapping("devices")
    public Result<List<EMapDeviceDto>> devices(Long productId, Long deviceGroupId) {
        List<EMapDeviceDto> devices = deviceService.listOfEMap(productId, deviceGroupId);
        return success(devices);
    }

    /**
     * 获取设备详情
     * @param uid
     * @return
     */
    @GetMapping("detail")
    public Result<EMapDetailDto> detail(String uid) {
        EMapDetailDto detailDto = new EMapDetailDto();
        deviceService.getByUid(uid).ifNotPresentThrow("设备不存在").ifPresent(item -> {
            productService.joinDetailById(item.getProductId())
                .ifNotPresentThrow("此设备数据不完整["+item.getName()+"]")
                .ifPresent(productDto -> {

                    if(!CollectionUtils.isEmpty(productDto.getFuncApis())) {
                        // 过滤出被动的功能接口
                        List<ModelApi> modelApis = productDto.getFuncApis().stream()
                                .filter(api -> api.getTriggerMode() == TriggerMode.passive && api.getFuncType() != FuncType.R)
                                .collect(Collectors.toList());
                        if(CollectionUtil.isNotEmpty(modelApis)) {
                            detailDto.setFuncApis(modelApis);
                            modelApis.forEach(api -> detailDto.addApiDebugParam(api.getCode(), api.resolveDownApiConfig(productDto.getAttrs())));
                            modelApis.removeIf(api -> {
                                if(Boolean.TRUE.equals(api.getAsStatus())) {
                                    detailDto.setCtrlApi(api);
                                    return true;
                                }

                                return false;
                            });
                        }
                    }

                    Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(productDto.getProtocolCode()
                            , DeviceKey.build(item.getDeviceSn(), item.getParentDeviceSn()));
                    if(CollectionUtil.isNotEmpty(productDto.getAttrs())) {
                        productDto.getAttrs().stream().filter(attr -> (attr.getAttrType() != AttrType.W
                            && !DataType.t_json.getValue().equals(attr.getDataType())))
                            .forEach(attr -> {
                                boolean isCtrlAttr = false;
                                if(attr.getAttrType() != AttrType.R) { // 写或者读写
                                    if(Boolean.TRUE.equals(attr.getCtrlStatus())) {
                                        isCtrlAttr = true;
                                        ModelApi ctrlApi = detailDto.getCtrlApi();
                                        if(ctrlApi != null) {
                                            List<ModelApiConfig> downConfig = ctrlApi.getDownConfig();
                                            if(CollectionUtil.isNotEmpty(downConfig)) {
                                                downConfig.forEach(config -> {
                                                    if(config.getModelAttrId() != null && config.getModelAttrId() == attr.getId()
                                                            && CollectionUtil.isNotEmpty(config.getDicts())) {
                                                        config.getDicts().forEach(dict -> detailDto.addCtrlAttr(dict.getDictName(), dict.getDictValue()));
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                if(realtimeDataMap != null) {
                                    RealtimeData realtimeData = realtimeDataMap.get(attr.getField());
                                    if(realtimeData != null && realtimeData.getRealtime() != null) {
                                        SignalOrFieldValue realtime = realtimeData.getRealtime();
                                        detailDto.addAttr(attr.getField(), attr.getName(), attr.getUnit()
                                                , realtime.getValue(), realtime.getCollectTime(), realtime.getStatus());

                                        if(isCtrlAttr && realtime.getValue() != null) {
                                            detailDto.setCtrlAttrValue(realtime.getValue().toString());
                                        }
                                        return;
                                    }
                                }

                                detailDto.addAttr(attr.getField(), attr.getName(), attr.getUnit());
                            });
                    }
                });
        });

        return success(detailDto);
    }
}
