package com.iteaj.iboot.module.iot.cache;

import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;

import java.util.Collection;
import java.util.Map;

public interface IotCacheManager {

    /**
     * 缓存设备信息
     * @param device
     * @return
     */
    RealtimeStatus cache(RealtimeStatus device);

    /**
     * 更新缓存信息
     * @param oldValue
     * @param newValue
     * @return
     */
    RealtimeStatus update(RealtimeStatus oldValue, RealtimeStatus newValue);

    /**
     * 移除指定组下面的所有缓存设备
     * @param groupId
     */
    Collection<RealtimeStatus> removeByGroupId(Long groupId);

    /**
     * 移除指定设备
     * @param device
     * @return
     */
    RealtimeStatus removeDevice(RealtimeStatus device);

    /**
     * 移除网关设备或者直连设备
     * @param protocolCode
     * @param deviceSn
     * @return
     */
    RealtimeStatus removeByKey(String protocolCode, String deviceSn);

    /**
     * 移除网关子设备
     * @param protocolCode
     * @param deviceSn
     * @param parentDeviceSn
     * @return
     */
    RealtimeStatus removeByKey(String protocolCode, String deviceSn, String parentDeviceSn);

    /**
     * 更新指定设备状态
     * @param protocolCode
     * @param key
     * @param status
     * @return
     */
    RealtimeStatus updateStatus(String protocolCode, DeviceKey key, DeviceStatus status);

    /**
     * 更新直连或者网关设备状态
     * @param protocolCode 协议码
     * @param deviceSn 设备编号
     * @param status 设备状态
     * @return
     */
    RealtimeStatus updateStatus(String protocolCode, String deviceSn, DeviceStatus status);

    /**
     * 更新网关子设备状态
     * @param protocolCode 协议码
     * @param deviceSn 设备编号
     * @param status 设备状态
     * @param parentDeviceSn 父设备编号
     * @return
     */
    RealtimeStatus updateStatus(String protocolCode, String deviceSn, String parentDeviceSn, DeviceStatus status);

    /**
     * 获取指定设备组下的指定uid的设备
     * @param uid 设备uid
     * @return
     */
    RealtimeStatus getByUid(String uid);

    /**
     * 获取指定的网关或者直连设备信息
     * @param protocolCode
     * @param deviceSn
     * @return
     */
    RealtimeStatus get(String protocolCode, String deviceSn);

    /**
     * 获取设备的实时数据
     * @param protocolCode
     * @param key
     * @return
     */
    RealtimeStatus get(String protocolCode, DeviceKey key);

    /**
     *  获取指定的网关子设备信息
     * @param protocolCode
     * @param deviceSn 设备编号
     * @param parentDeviceSn 父设备编号(网关设备编号)
     * @return
     */
    RealtimeStatus get(String protocolCode, String deviceSn, String parentDeviceSn);

    /**
     * 获取网关或者直连设备信息
     * @param protocolCode
     * @param deviceSn
     * @return
     */
    Map<String, RealtimeStatus> list(String protocolCode, String... deviceSn);

    /**
     * 获取指定网关设备下的所有子设备
     * @param protocolCode
     * @param parentDeviceSn 网关设备编号
     * @param deviceSn
     * @return
     */
    Map<String, RealtimeStatus> listForParent(String protocolCode, String parentDeviceSn, String... deviceSn);

    /**
     * 获取指定组下面的所有设备
     * @param id
     * @return
     */
    Collection<RealtimeStatus> listByGroupId(Long id);

    /**
     * 获取指定产品下面的所有设备
     * @param productCode
     * @return
     */
    Collection<RealtimeStatus> listByProductCode(String productCode);

    /**
     * 获取指定组下面的指定产品的所有设备
     * @param groupId
     * @param productCode
     * @return
     */
    Collection<RealtimeStatus> listGroupAndProduct(Long groupId, String productCode);

    /**
     * 更新网关设备下所有网关子设备的状态
     * @param protocolCode
     * @param parentDeviceSn
     * @param status
     */
    void updateChildStatus(String protocolCode, String parentDeviceSn, DeviceStatus status);
}
