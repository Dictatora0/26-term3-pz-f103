package com.iteaj.iboot.module.iot.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.CheckRole;
import com.iteaj.framework.security.CheckScope;
import com.iteaj.framework.security.Logical;
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
 *
 * @see RealtimePushListener websocket实时数据监听
 */
@RestController
@RequestMapping("/iot/realtime")
@CheckPermission("iot:device:view")
@CheckRole(value = {"管理员", "OPERATOR", "VIEWER"}, logical = Logical.OR)
@CheckScope("device.read")
public class RealtimeDataController extends BaseController {

    private final IDeviceService deviceService;
    private final RealtimeDataService realtimeDataService;

    public RealtimeDataController(IDeviceService deviceService, RealtimeDataService realtimeDataService) {
        this.deviceService = deviceService;
        this.realtimeDataService = realtimeDataService;
    }

    @GetMapping("listByDeviceId")
    public Result<Map<String, Object>> listByDevice(Long deviceId) {
        DeviceDto device = deviceService.detailById(deviceId).ifNotPresentThrow("设备不存在").getData();
        Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(
                device.getProductCode(), DeviceKey.build(device.getDeviceSn(), device.getDeviceSn()));
        if (realtimeDataMap != null) {
            Map<String, Object> result = new HashMap<>();
            realtimeDataMap.forEach((key, value) -> result.put(key, value.getRealtime()));
            return success(result);
        }
        return success(Collections.emptyMap());
    }

    @GetMapping("listByDevice")
    public Result<Map<String, Object>> listByDevice(String productCode, String deviceSn) {
        Map<String, RealtimeData> realtimeDataMap = realtimeDataService.listOfDevice(productCode, DeviceKey.build(deviceSn, null));
        if (realtimeDataMap != null) {
            Map<String, Object> result = new HashMap<>();
            realtimeDataMap.forEach((key, value) -> result.put(key, value.getRealtime()));
            return success(result);
        }
        return success(Collections.emptyMap());
    }

    @GetMapping("get")
    public Result<RealtimeData> getByEventOrSignalField(String productCode, String deviceSn, String eventOrSignalField) {
        RealtimeData realtimeData = realtimeDataService.getOfDeviceAndKey(productCode, DeviceKey.build(deviceSn, null), eventOrSignalField);
        return success(realtimeData);
    }

    @GetMapping("getByDeviceId")
    public Result<RealtimeData> getByEventOrSignalField(Long deviceId, String signalOrField) {
        DeviceDto device = deviceService.detailById(deviceId).ifNotPresentThrow("设备不存在").getData();
        RealtimeData realtimeData = realtimeDataService.getOfDeviceAndKey(
                device.getProductCode(), DeviceKey.build(device.getDeviceSn(), device.getDeviceSn()), signalOrField);
        return success(realtimeData);
    }
}
