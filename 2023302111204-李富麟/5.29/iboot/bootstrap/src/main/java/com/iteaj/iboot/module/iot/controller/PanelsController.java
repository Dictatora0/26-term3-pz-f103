package com.iteaj.iboot.module.iot.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.consts.AttrType;
import com.iteaj.framework.spi.iot.consts.DataType;
import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.data.RealtimeData;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.model.DeviceStatusModelApiManager;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.DeviceDto;
import com.iteaj.iboot.module.iot.dto.PanelsDetailDto;
import com.iteaj.iboot.module.iot.dto.PanelsDeviceDto;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.dto.StatusSwitchDto;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import com.iteaj.iboot.module.iot.entity.Signal;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iboot.module.iot.service.ISignalService;
import com.iteaj.iboot.module.iot.utils.ProtocolInvokeUtil;
import com.iteaj.iot.consts.ExecStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运行展板模块
 */
@RestController
@RequestMapping("/iot/panels")
public class PanelsController extends BaseController {

    private final ISignalService signalService;
    private final IDeviceService deviceService;
    private final IProductService productService;
    private final IotCacheManager cacheManager;
    private final RealtimeDataService realtimeDataService;

    public PanelsController(ISignalService signalService, IDeviceService deviceService
            , IProductService productService, IotCacheManager cacheManager
            , RealtimeDataService realtimeDataService) {
        this.signalService = signalService;
        this.deviceService = deviceService;
        this.productService = productService;
        this.cacheManager = cacheManager;
        this.realtimeDataService = realtimeDataService;
    }

    /**
     * 设备列表
     *
     * @param deviceGroupId 设备分组
     * @return
     */
    @GetMapping("/devices")
    public Result<IPage<PanelsDeviceDto>> devices(Page page, Long deviceGroupId, Long productId, DeviceType deviceType) {
        DeviceDto device = new DeviceDto();
        device.setProductId(productId);
        device.setDeviceGroupId(deviceGroupId);
        device.setDeviceType(deviceType);
        PageResult<IPage<DeviceDto>> pageResult = deviceService.pageOfDetail(page, device);

        Map<Long, String> productProtocolMap = new HashMap<>();
        Map<Long, List<IVOption>> productOptionsMap = new HashMap<>();
        Map<Long, Map<String, String>> productAttrDictsMap = new HashMap<>();
        Map<Long, List<PanelsDeviceDto.AttrValue>> productValuesMap = new HashMap<>();

        IPage<DeviceDto> data = pageResult.getData();
        if (CollectionUtil.isNotEmpty(data.getRecords())) {
            List<Long> productIds = data.getRecords().stream()
                    .map(item -> item.getProductId()).distinct()
                    .collect(Collectors.toList());
            List<ProductDto> list = productService.listCtrlStatusModelAttrs(productIds);
            if (CollectionUtil.isNotEmpty(list)) {
                list.forEach(productDto -> {
                    List<IVOption> options = new ArrayList<>();
                    List<PanelsDeviceDto.AttrValue> values = new ArrayList<>();
                    productProtocolMap.put(productDto.getId(), productDto.getProtocolCode());
                    if (CollectionUtil.isNotEmpty(productDto.getAttrs())) {
                        Map<String, String> attrDictMap = new HashMap<>();
                        productDto.getAttrs().forEach(attr -> {
                            if (attr.getAttrType() != AttrType.W) { // 读和读写
                                if (!DataType.t_json.getValue().equals(attr.getDataType())) {
                                    values.add(new PanelsDeviceDto.AttrValue(attr.getField(), attr.getName(), attr.getUnit(), attr.getCtrlStatus()));
                                }
                            }

                            if (attr.getAttrType() != AttrType.R) { // 写或者读写
                                if (Boolean.TRUE.equals(attr.getCtrlStatus()) && CollectionUtil.isNotEmpty(attr.getDicts())) {
                                    attr.getDicts().forEach(dict -> options.add(new IVOption(dict.getDictName(), dict.getDictValue())));
                                }
                            }

                            if (CollectionUtil.isNotEmpty(attr.getDicts()) && !DataType.t_json.getValue().equals(attr.getDataType())) {
                                attr.getDicts().forEach(dict -> attrDictMap.put(attr.getField() + "_" + dict.getDictValue(), dict.getDictName()));
                            }
                        });

                        productValuesMap.put(productDto.getId(), values);
                        productOptionsMap.put(productDto.getId(), options);
                        productAttrDictsMap.put(productDto.getId(), attrDictMap);
                    }
                });

                List<PanelsDeviceDto> panelsDeviceDtos = new ArrayList<>();
                data.getRecords().forEach(deviceDto -> {
                    deviceDto.setConfig(null);
                    List<IVOption> options = productOptionsMap.get(deviceDto.getProductId());
                    Map<String, String> dictStringMap = productAttrDictsMap.get(deviceDto.getProductId());
                    List<PanelsDeviceDto.AttrValue> attrValues = productValuesMap.get(deviceDto.getProductId());

                    PanelsDeviceDto panelsDeviceDto = new PanelsDeviceDto(options, Collections.EMPTY_LIST, deviceDto);
                    // 获取每个属性的实时数据
                    if (CollectionUtil.isNotEmpty(attrValues)) {
                        List<PanelsDeviceDto.AttrValue> copyAttrValues = attrValues.stream()
                                .map(item -> new PanelsDeviceDto.AttrValue(item.getField(), item.getName(), item.getUnit(), item.getCtrlStatus()))
                                .collect(Collectors.toList());

                        copyAttrValues.forEach(attrValue -> {
                            String protocolCode = productProtocolMap.get(deviceDto.getProductId());
                            Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(protocolCode
                                    , DeviceKey.build(deviceDto.getDeviceSn(), deviceDto.getParentDeviceSn()));
                            if (CollectionUtil.isNotEmpty(realtimeDataMap)) {

                                // 设置各属性的实时值
                                RealtimeData realtimeData = realtimeDataMap.get(attrValue.getField());
                                if (realtimeData != null && realtimeData.getRealtime() != null
                                        && realtimeData.getRealtime().getValue() != null) {

                                    String value = realtimeData.getRealtime().getValue().toString();
                                    attrValue.setValue(value);

                                    // 将采集到的值转成字典label
                                    attrValue.setLabel(dictStringMap.get(attrValue.getField() + "_" + value));

                                    // 设置控制属性的值
                                    if (Boolean.TRUE.equals(attrValue.getCtrlStatus())) {
                                        panelsDeviceDto.setCtrlValue(value);
                                    }
                                }
                            }
                        });

                        panelsDeviceDto.setValues(copyAttrValues);
                    }

                    panelsDeviceDtos.add(panelsDeviceDto);
                });

                page.setRecords(panelsDeviceDtos);
            }
        }
        return success(page);
    }

    /**
     * 调试详情
     *
     * @param deviceId
     * @return
     */
    @GetMapping("detail")
    public Result<PanelsDetailDto> detail(Long deviceId) {
        PanelsDetailDto debugDto = new PanelsDetailDto();
        deviceService.detailById(deviceId)
                .ifNotPresentThrow("设备不存在").of().ifPresent(item -> {
                    debugDto.setDevice(item);
                    productService.joinDetailById(item.getProductId())
                            .ifNotPresentThrow("此设备数据不完整[" + item.getName() + "]")
                            .ifPresent(productDto -> {
                                debugDto.setProduct(productDto);
                                if (!CollectionUtils.isEmpty(productDto.getFuncApis())) {
                                    // 过滤出被动的功能接口
                                    List<ModelApi> modelApis = productDto.getFuncApis().stream()
                                            .filter(api -> api.getTriggerMode() == TriggerMode.passive && !Boolean.TRUE.equals(api.getAsStatus()))
                                            .collect(Collectors.toList());
                                    if (CollectionUtil.isNotEmpty(modelApis)) {
                                        modelApis.forEach(api -> debugDto.addApiDebugParam(api.getCode(), api.resolveDownApiConfig(productDto.getAttrs())));
                                    }

                                    productDto.setFuncApis(modelApis);
                                }

                                if (!CollectionUtils.isEmpty(productDto.getEventApis())) {
                                    // 过滤出被动的事件接口
                                    List<ModelApi> modelApis = productDto.getEventApis().stream()
                                            .filter(api -> api.getTriggerMode() == TriggerMode.passive)
                                            .collect(Collectors.toList());
                                    productDto.setEventApis(modelApis);
                                }
                                Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(productDto.getProtocolCode()
                                        , DeviceKey.build(item.getDeviceSn(), item.getParentDeviceSn()));
                                if (CollectionUtil.isNotEmpty(productDto.getAttrs())) {
                                    productDto.getAttrs().stream().filter(attr -> (attr.getAttrType() != AttrType.W
                                                    && !DataType.t_json.getValue().equals(attr.getDataType())))
                                            .forEach(attr -> {
                                                if (realtimeDataMap != null) {
                                                    RealtimeData realtimeData = realtimeDataMap.get(attr.getField());
                                                    if (realtimeData != null && realtimeData.getRealtime() != null) {
                                                        SignalOrFieldValue realtime = realtimeData.getRealtime();
                                                        debugDto.addAttr(attr.getField(), attr.getName(), attr.getUnit(), attr.getAttrType()
                                                                , realtime.getValue(), realtime.getCollectTime(), realtime.getStatus());
                                                        return;
                                                    }
                                                }

                                                debugDto.addAttr(attr.getField(), attr.getName(), attr.getUnit(), attr.getAttrType());
                                            });
                                }

                                productDto.setAttrs(null);

                                // 处理点位数据
                                signalService.list(Wrappers.<Signal>lambdaQuery().eq(Signal::getProductId, productDto.getId()))
                                        .stream().forEach(signal -> {
                                            if (realtimeDataMap != null) {
                                                RealtimeData realtimeData = realtimeDataMap.get(signal.getAddress());
                                                if (realtimeData != null && realtimeData.getRealtime() != null) {
                                                    SignalOrFieldValue realtime = realtimeData.getRealtime();
                                                    debugDto.addSignal(signal.getName(), signal.getAddress(), realtime.getValue()
                                                            , realtime.getCollectTime(), realtime.getStatus());
                                                    return;
                                                }
                                            }

                                            debugDto.addSignal(signal.getName(), signal.getAddress());
                                        });
                            });
                });

        return success(debugDto);
    }

    /**
     * 切换控制状态
     *
     * @param dto id为设备的uid值
     * @return
     */
    @PostMapping("switchCtrlStatus")
    public Result switchCtrlStatus(@RequestBody StatusSwitchDto dto) {
        this.deviceService.getByUid(dto.getId().toString())
                .ifNotPresentThrow("设备不存在")
                .ifPresent(item -> {
                    this.productService.getCtrlStatusModelApi(item.getProductId())
                            .ifNotPresentThrow("此设备绑定的产品有误").ifPresent(productDto -> {
                                if (CollectionUtil.isEmpty(productDto.getFuncApis())) {
                                    throw new ServiceException("此产品[" + productDto.getName() + "]没有指定状态接口");
                                }

                                if (productDto.getStatus() == FuncStatus.disabled) {
                                    throw new ServiceException("请先启用产品[" + productDto.getName() + "]");
                                }

                                ModelApi modelApi = productDto.getFuncApis().get(0);
                                DeviceKey deviceKey = DeviceKey.build(item.getDeviceSn(), item.getParentDeviceSn());
                                RealtimeStatus realtimeStatus = cacheManager.get(productDto.getProtocolCode(), deviceKey);

                                if (CollectionUtil.isEmpty(productDto.getAttrs())) {
                                    throw new ServiceException("此产品[" + productDto.getName() + "]没有模型属性");
                                }

                                ModelAttr ctrlStatusAttr = productDto.getAttrs().stream()
                                        .filter(attr -> Boolean.TRUE.equals(attr.getCtrlStatus()))
                                        .findFirst().orElse(null);
                                if (ctrlStatusAttr == null) {
                                    throw new ServiceException("此产品[" + productDto.getName() + "]没有设置控制属性");
                                }

                                JSONObject jsonObject = new JSONObject();
                                ProtocolModelApiInvokeParam invokeParam = new ProtocolModelApiInvokeParam(realtimeStatus.getUid(), jsonObject);
                                if (CollectionUtil.isNotEmpty(modelApi.getDownConfig())) {
                                    modelApi.getDownConfig().forEach(config -> {
                                        if (config.getModelAttrId() != null) {
                                            // 控制属性
                                            if (ctrlStatusAttr.getField().equals(config.getAttrField())) {
                                                jsonObject.put(config.getProtocolAttrField(), dto.getStatus());
                                            } else {
                                                jsonObject.put(config.getProtocolAttrField(), config.getAttrDefaultValue());
                                            }
                                        } else {
                                            jsonObject.put(config.getProtocolAttrField(), config.getValue());
                                        }
                                    });
                                }

                                ProtocolInvokeUtil.invoke(modelApi, invokeParam, realtimeStatus, (result) -> {
                                    if (result.getStatus() != ExecStatus.success) {
                                        throw new ServiceException("调用设备失败[" + result.getReason() + "]");
                                    } else {
                                        // 将实时值更新为下发的值
                                        realtimeDataService.put(realtimeStatus.getProtocolCode(), deviceKey
                                                , SignalOrFieldValue.build(ctrlStatusAttr.getId(), ctrlStatusAttr.getField(), dto.getStatus()));

                                        // 触发状态更新
                                        DeviceStatusModelApiManager.getInstance().trigger(realtimeStatus.getProductCode(), realtimeStatus.getUid());
                                    }
                                });
                            });
                });

        return success();
    }

    /**
     * 设备列表
     *
     * @param deviceGroupId 设备分组
     * @return
     */
    @GetMapping("/mqtt/devices")
    public Result<IPage<PanelsDeviceDto>> mqttDevices(Page page, Long deviceGroupId, Long productId, DeviceType deviceType) {
        DeviceDto device = new DeviceDto();
        device.setProductId(productId);
        device.setDeviceGroupId(deviceGroupId);
        device.setDeviceType(deviceType);
        device.setProtocolCodes(new String[]{ProtocolCodes.MQTT_DEFAULT_IMPL.getValue()});
        PageResult<IPage<DeviceDto>> pageResult = deviceService.pageOfDetail(page, device);

        Map<Long, String> productProtocolMap = new HashMap<>();
        Map<Long, List<IVOption>> productOptionsMap = new HashMap<>();
        Map<Long, Map<String, String>> productAttrDictsMap = new HashMap<>();
        Map<Long, List<PanelsDeviceDto.AttrValue>> productValuesMap = new HashMap<>();

        IPage<DeviceDto> data = pageResult.getData();
        if (CollectionUtil.isNotEmpty(data.getRecords())) {
            List<Long> productIds = data.getRecords().stream()
                    .map(item -> item.getProductId()).distinct()
                    .collect(Collectors.toList());
            List<ProductDto> list = productService.listCtrlStatusModelAttrs(productIds);
            if (CollectionUtil.isNotEmpty(list)) {
                list.forEach(productDto -> {
                    List<IVOption> options = new ArrayList<>();
                    List<PanelsDeviceDto.AttrValue> values = new ArrayList<>();
                    productProtocolMap.put(productDto.getId(), productDto.getProtocolCode());
                    if (CollectionUtil.isNotEmpty(productDto.getAttrs())) {
                        Map<String, String> attrDictMap = new HashMap<>();
                        productDto.getAttrs().forEach(attr -> {
                            if (attr.getAttrType() != AttrType.W) { // 读和读写
                                if (!DataType.t_json.getValue().equals(attr.getDataType())) {
                                    values.add(new PanelsDeviceDto.AttrValue(attr.getField(), attr.getName(), attr.getUnit(), attr.getCtrlStatus()));
                                }
                            }

                            if (attr.getAttrType() != AttrType.R) { // 写或者读写
                                if (Boolean.TRUE.equals(attr.getCtrlStatus()) && CollectionUtil.isNotEmpty(attr.getDicts())) {
                                    attr.getDicts().forEach(dict -> options.add(new IVOption(dict.getDictName(), dict.getDictValue())));
                                }
                            }

                            if (CollectionUtil.isNotEmpty(attr.getDicts()) && !DataType.t_json.getValue().equals(attr.getDataType())) {
                                attr.getDicts().forEach(dict -> attrDictMap.put(attr.getField() + "_" + dict.getDictValue(), dict.getDictName()));
                            }
                        });

                        productValuesMap.put(productDto.getId(), values);
                        productOptionsMap.put(productDto.getId(), options);
                        productAttrDictsMap.put(productDto.getId(), attrDictMap);
                    }
                });

                List<PanelsDeviceDto> panelsDeviceDtos = new ArrayList<>();
                data.getRecords().forEach(deviceDto -> {
                    deviceDto.setConfig(null);
                    List<IVOption> options = productOptionsMap.get(deviceDto.getProductId());
                    Map<String, String> dictStringMap = productAttrDictsMap.get(deviceDto.getProductId());
                    List<PanelsDeviceDto.AttrValue> attrValues = productValuesMap.get(deviceDto.getProductId());

                    PanelsDeviceDto panelsDeviceDto = new PanelsDeviceDto(options, Collections.EMPTY_LIST, deviceDto);
                    // 获取每个属性的实时数据
                    if (CollectionUtil.isNotEmpty(attrValues)) {
                        List<PanelsDeviceDto.AttrValue> copyAttrValues = attrValues.stream()
                                .map(item -> new PanelsDeviceDto.AttrValue(item.getField(), item.getName(), item.getUnit(), item.getCtrlStatus()))
                                .collect(Collectors.toList());

                        copyAttrValues.forEach(attrValue -> {
                            String protocolCode = productProtocolMap.get(deviceDto.getProductId());
                            Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(protocolCode
                                    , DeviceKey.build(deviceDto.getDeviceSn(), deviceDto.getParentDeviceSn()));
                            if (CollectionUtil.isNotEmpty(realtimeDataMap)) {

                                // 设置各属性的实时值
                                RealtimeData realtimeData = realtimeDataMap.get(attrValue.getField());
                                if (realtimeData != null && realtimeData.getRealtime() != null
                                        && realtimeData.getRealtime().getValue() != null) {

                                    String value = realtimeData.getRealtime().getValue().toString();
                                    attrValue.setValue(value);

                                    // 将采集到的值转成字典label
                                    attrValue.setLabel(dictStringMap.get(attrValue.getField() + "_" + value));

                                    // 设置控制属性的值
                                    if (Boolean.TRUE.equals(attrValue.getCtrlStatus())) {
                                        panelsDeviceDto.setCtrlValue(value);
                                    }
                                }
                            }
                        });

                        panelsDeviceDto.setValues(copyAttrValues);
                    }

                    productService.debugById(deviceDto.getProductId())
                            .ifNotPresentThrow("此设备数据不完整[" + deviceDto.getName() + "]")
                            .ifPresent(productDto -> {
                                panelsDeviceDto.setProduct(productDto);
                                productDto.setAttrs(null);
                            });

                    panelsDeviceDtos.add(panelsDeviceDto);
                });

                page.setRecords(panelsDeviceDtos);
            }
        }
        return success(page);
    }

}
