package com.iteaj.iboot.module.iot.cache.data;

import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.data.IModelAttrRealtimeDataService;

import java.util.List;
import java.util.Map;

/**
 * 实时数据服务
 */
public interface RealtimeDataService extends IModelAttrRealtimeDataService {

    /**
     * 移除指定协议下得所有数据
     * @param protocolCode
     */
    void remove(String protocolCode);

    /**
     * 移除指定协议下指定设备得所有信息
     * @param protocolCode
     * @param key
     */
    Map<String, RealtimeData> remove(String protocolCode, DeviceKey key);

    /**
     * 移除指定协议下指定设备下得事件或者点位字段信息
     * @param protocolCode
     * @param key
     * @param signalOrField
     */
    RealtimeData remove(String protocolCode, DeviceKey key, String signalOrField);


    /**
     * 移除指定协议下指定设备下得事件或者点位字段信息
     * @param protocolCode
     * @param key
     * @param signalOrFields
     */
    Map<String, RealtimeData> remove(String protocolCode, DeviceKey key, String... signalOrFields);

    /**
     * 缓存数据
     * @param protocolCode 产品代码
     * @param key 直连设备或者边缘网关设备的设备编号(或网关设备编号和网关子设备编号的组合)
     * @param value 事件代码或者点位字段
     * @param value
     */
    void put(String protocolCode, DeviceKey key, SignalOrFieldValue value);

    /**
     * 缓存数据
     * @param supplier
     */
    void put(DataSupplier supplier);

    /**
     * 缓存数据
     * @param suppliers 提供数据列表
     */
    void put(List<DataSupplier> suppliers);

    /**
     * 获取指定设备下面的所有实时数据
     * @param protocolCode
     * @param key 设备编号
     * @return
     */
    Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key);

    /**
     * 获取指定设备下指定事件的实时数据
     * @param protocolCode
     * @param key 设备编号
     * @param signalOrField 事件代码或者点位字段
     * @return
     */
    RealtimeData getOfDeviceAndKey(String protocolCode, DeviceKey key, String signalOrField);

    /**
     * 获取指定设备下面的所有实时数据
     * @param protocolCode
     * @param key 设备编号
     * @param signalOrFields 事件代码或者点位字段列表
     * @return
     */
    Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key, String... signalOrFields);

}
