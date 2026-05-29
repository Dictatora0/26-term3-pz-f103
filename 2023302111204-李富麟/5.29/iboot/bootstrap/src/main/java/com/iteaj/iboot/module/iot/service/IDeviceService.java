package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.*;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.dto.*;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.framework.IBaseService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 设备表 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
public interface IDeviceService extends IBaseService<Device> {

    /**
     * 获取指定的网关或者直连设备信息
     * @param protocolCode
     * @param deviceSn
     * @return
     */
    RealtimeStatus getDeviceStatusFromCache(String protocolCode, String deviceSn);

    /**
     *  获取指定的网关子设备信息
     * @param protocolCode
     * @param deviceSn 设备编号
     * @param parentDeviceSn 父设备编号(网关设备编号)
     * @return
     */
    RealtimeStatus getDeviceStatusFromCache(String protocolCode, String deviceSn, String parentDeviceSn);

    /**
     * 获取网关或者直连设备信息
     * @param protocolCode
     * @param deviceSn
     * @return
     */
    Map<String, RealtimeStatus> listDeviceStatusFromCache(String protocolCode, String... deviceSn);

    /**
     * 获取指定网关设备下的所有子设备
     * @param protocolCode
     * @param parentDeviceSn 网关设备编号
     * @param deviceSn 子设备编号
     * @return
     */
    Map<String, RealtimeStatus> listChildDeviceStatusFromCache(String protocolCode, String parentDeviceSn, String... deviceSn);

    /**
     * 获取设备详情分页列表
     * @param page
     * @param entity
     * @return
     */
    PageResult<IPage<DeviceDto>> pageOfDetail(Page<Device> page, DeviceDto entity);

    /**
     * 更新设备状态
     * @param deviceSn
     * @param status
     */
    void update(String deviceSn, DeviceStatus status);

    /**
     * 获取指定设备编号的记录
     * @param deviceSn
     * @return
     */
    DetailResult<Device> getByDeviceSn(String deviceSn);

    /**
     * 统计当前设备信息
     * @return
     */
    CurrentDeviceDto countCurrentDevice();

    /**
     * 获取设备详情
     * @param id
     * @return
     */
    DetailResult<DeviceDto> detailById(Long id);

    /**
     * 获取网关设备
     * @return
     * @param gatewayId
     */
    ListResult<Device> listOfGateway(Long gatewayId);

    /**
     * 获取指定产品下面的所有设备
     * @param productId
     * @return
     */
    ListResult<DeviceDto> listByProductId(Long productId);

    /**
     * 是否已经存在设备编号
     * @param deviceSn 要校验的设备编号
     * @param protocolCode 协议码
     * @param gatewayDeviceSn 边缘网关的设备编号
     * @return
     */
    DetailResult<Device> getByDeviceSn(String deviceSn, String protocolCode, String gatewayDeviceSn);

    /**
     * 获取设备的缓存信息
     * @param deviceId
     * @return
     */
    RealtimeStatus getDeviceStatusFromCache(Long deviceId);

    /**
     * @param idList 设备id列表
     * @return
     */
    List<RealtimeStatus> listDeviceStatusById(Collection<?> idList);

    /**
     * 获取所有设备的缓存信息
     * @return
     */
    ListResult<RealtimeStatus> listDeviceStatus();

    /**
     * 更新设备状态
     * @param device
     */
    void updateDeviceStatus(RealtimeStatus device);

    RealtimeStatus getDeviceStatus(String protocolCode, String deviceSn, String parentDeviceSn);

    DetailResult<Device> getByUid(String uid);

    /**
     * 统计设备的在线离线状态
     * @param param
     * @return
     */
    DeviceStatusDto countDeviceStatus(GroupAndProductParamDto param);

    /**
     * 电子地图功能的设备列表
     * @param productId
     * @param deviceGroupId
     * @return
     */
    List<EMapDeviceDto> listOfEMap(Long productId, Long deviceGroupId);
}
