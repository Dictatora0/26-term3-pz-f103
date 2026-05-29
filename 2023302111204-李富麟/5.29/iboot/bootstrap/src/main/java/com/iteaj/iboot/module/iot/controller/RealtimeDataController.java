package com.iteaj.iboot.module.iot.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.iboot.module.iot.cache.data.RealtimeData;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.collect.websocket.RealtimePushListener;
import com.iteaj.iboot.module.iot.dto.DeviceDto;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 实时数据管理
 * @see RealtimePushListener websocket实时数据监听
 */
@RestController
@RequestMapping("/iot/realtime")
public class RealtimeDataController extends BaseController {

    private final IDeviceService deviceService;
    private final RealtimeDataService realtimeDataService;

    public RealtimeDataController(IDeviceService deviceService, RealtimeDataService realtimeDataService) {
        this.deviceService = deviceService;
        this.realtimeDataService = realtimeDataService;
    }

    /**
     * 获取指定设备下面的所有事件或者点位数据
     * @param deviceId
     * @return
     */
    @GetMapping("listByDeviceId")
    public Result<Map<String, Object>> listByDevice(Long deviceId) {
        DeviceDto device = deviceService.detailById(deviceId).ifNotPresentThrow("设备不存在").getData();
        Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(device.getProductCode(), DeviceKey.build(device.getDeviceSn(), device.getDeviceSn()));
        if(realtimeDataMap != null) {
            Map<String, Object> result = new HashMap<>();
            realtimeDataMap.entrySet().forEach(entry -> {
                result.put(entry.getKey(), entry.getValue().getRealtime());
            });
            return success(result);
        }

        return success(Collections.EMPTY_MAP);
    }

    /**
     * 获取指定设备下面的所有事件或者点位数据
     * @param productCode
     * @param deviceSn
     * @return
     */
    @GetMapping("listByDevice")
    public Result<Map<String, Object>> listByDevice(String productCode, String deviceSn) {
        Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(productCode, DeviceKey.build(deviceSn, null));
        if(realtimeDataMap != null) {
            Map<String, Object> result = new HashMap<>();
            realtimeDataMap.entrySet().forEach(entry -> {
                result.put(entry.getKey(), entry.getValue().getRealtime());
            });
            return success(result);
        }

        return success(Collections.EMPTY_MAP);
    }

    /**
     * 获取指定设备点位或者字段实时数据
     * @param productCode
     * @param deviceSn
     * @param eventOrSignalField 事件代码或者点位字段
     * @return
     */
    @GetMapping("get")
    public Result<RealtimeData> getByEventOrSignalField(String productCode, String deviceSn, String eventOrSignalField) {
        RealtimeData realtimeData = realtimeDataService.getOfDeviceAndKey(productCode, DeviceKey.build(deviceSn, null), eventOrSignalField);
        return success(realtimeData);
    }

    /**
     * 获取指定设备下某个事件或者点位字段的实时数据
     * @param deviceId
     * @param signalOrField 事件代码或者点位字段
     * @return
     */
    @GetMapping("getByDeviceId")
    public Result<RealtimeData> getByEventOrSignalField(Long deviceId, String signalOrField) {
        DeviceDto device = deviceService.detailById(deviceId).ifNotPresentThrow("设备不存在").getData();
        RealtimeData realtimeData = realtimeDataService.getOfDeviceAndKey(device.getProductCode()
                , DeviceKey.build(device.getDeviceSn(), device.getDeviceSn()), signalOrField);
        return success(realtimeData);
    }
}
