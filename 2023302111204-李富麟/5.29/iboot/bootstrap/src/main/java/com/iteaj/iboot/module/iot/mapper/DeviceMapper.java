package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.dto.*;
import com.iteaj.iboot.module.iot.entity.Device;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 设备表 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 获取设备详情分页列表
     * @param page
     * @param entity
     * @return
     */
    IPage<DeviceDto> pageOfDetail(Page<Device> page, DeviceDto entity);

    /**
     * 统计当前设备信息
     * @return
     */
    CurrentDeviceDto countCurrentDevice();

    DeviceDto detailById(Long id);

    /**
     * 获取网关设备
     * @see com.iteaj.iboot.module.iot.consts.DeviceType#Gateway
     * @return
     * @param gatewayId
     */
    List<Device> listOfGateway(Long gatewayId);

    List<DeviceDto> listByProductId(Long productId);

    /**
     * 是否已经存在设备编号
     * @param deviceSn 要校验的设备编号
     * @param protocolCode 协议码
     * @param gatewayDeviceSn 边缘网关的设备编号
     * @return
     */
    Device getByDeviceSn(String deviceSn, String protocolCode, String gatewayDeviceSn);

    /**
     * 获取设备的缓存信息
     * @param deviceId
     * @return
     */
    RealtimeStatus getDeviceCacheById(Long deviceId);

    List<RealtimeStatus> listDeviceStatusById(Collection<?> list);

    List<RealtimeStatus> listDeviceCache();

    RealtimeStatus getDeviceStatus(String protocolCode, String deviceSn, String parentDeviceSn);

    List<RealtimeStatus> listDeviceStatus(String protocolCode, List<String> deviceSns, String parentDeviceSn);

    Device getByUid(String uid);

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
