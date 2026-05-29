package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.*;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectListenerManager;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.*;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.mapper.DeviceMapper;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iboot.module.iot.utils.IotNetworkUtil;
import com.iteaj.iot.client.IotClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备表 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
@Service
public class DeviceServiceImpl extends BaseServiceImpl<DeviceMapper, Device> implements IDeviceService {

    private final IProductService productService;
    private final IotCacheManager iotCacheManager;
    private final RealtimeDataService realtimeDataService;
    public DeviceServiceImpl(IProductService productService, IotCacheManager iotCacheManager
            , RealtimeDataService realtimeDataService) {
        this.productService = productService;
        this.iotCacheManager = iotCacheManager;
        this.realtimeDataService = realtimeDataService;
    }


    @Override
    public RealtimeStatus getDeviceStatusFromCache(String protocolCode, String deviceSn) {
        RealtimeStatus realtimeStatus = iotCacheManager.get(protocolCode, deviceSn);
        if(realtimeStatus == null) {
            realtimeStatus = this.getDeviceStatus(protocolCode, deviceSn, null);
            if(realtimeStatus != null) {
                iotCacheManager.cache(realtimeStatus);
            }
        }

        return realtimeStatus;
    }

    @Override
    public RealtimeStatus getDeviceStatusFromCache(String protocolCode, String deviceSn, String parentDeviceSn) {
        RealtimeStatus realtimeStatus = iotCacheManager.get(protocolCode, deviceSn, parentDeviceSn);
        if(realtimeStatus == null) {
            realtimeStatus = this.getDeviceStatus(protocolCode, deviceSn, parentDeviceSn);
            if(realtimeStatus != null) {
                iotCacheManager.cache(realtimeStatus);
            }
        }

        return realtimeStatus;
    }

    @Override
    public Map<String, RealtimeStatus> listDeviceStatusFromCache(String protocolCode, String... deviceSn) {
        Map<String, RealtimeStatus> statusMap = iotCacheManager.list(protocolCode, deviceSn);
        if(CollectionUtil.isEmpty(statusMap)) {
            List<RealtimeStatus> statuses = this.getBaseMapper().listDeviceStatus(protocolCode, Arrays.asList(deviceSn), null);
            if(CollectionUtil.isNotEmpty(statuses)) {
                statuses.forEach(status -> {
                    iotCacheManager.cache(status);
                    statusMap.put(status.getDeviceSn(), status);
                });
            }
        }
        return statusMap;
    }

    @Override
    public Map<String, RealtimeStatus> listChildDeviceStatusFromCache(String protocolCode, String parentDeviceSn, String... deviceSn) {
        Map<String, RealtimeStatus> statusMap = iotCacheManager.listForParent(protocolCode, parentDeviceSn, deviceSn);
        if(CollectionUtil.isEmpty(statusMap)) {
            List<RealtimeStatus> statuses = this.getBaseMapper().listDeviceStatus(protocolCode, Arrays.asList(deviceSn), parentDeviceSn);
            if(CollectionUtil.isNotEmpty(statuses)) {
                statuses.forEach(status -> {
                    iotCacheManager.cache(status);
                    statusMap.put(status.getDeviceSn(), status);
                });
            }
        }
        return statusMap;
    }

    @Override
    public PageResult<IPage<DeviceDto>> pageOfDetail(Page<Device> page, DeviceDto entity) {
        return new PageResult<>(getBaseMapper().pageOfDetail(page, entity));
    }

    @Override
    @Transactional
    public BooleanResult save(Device entity) {
        validate(entity, null);
        entity.setUid(String.valueOf(IdWorker.getId()));

        // 将uid写入配置, 作为设备的唯一键
        if(entity.getConfig() != null) {
            entity.getConfig().put("uid", entity.getUid());
        }

        return super.save(entity).ifPresentAndTrue(item -> {
            RealtimeStatus status = this.getDeviceStatusFromCache(entity.getId());
            iotCacheManager.cache(status);
        });
    }

    private void validate(Device entity, RealtimeStatus oldStatus) {
        productService.detailById(entity.getProductId()).ifNotPresentThrow("产品不存在").ifPresent(productDto -> {
            DeviceType deviceType = productDto.getDeviceType();
            // 网关子设备在同一网关设备下的设备编号必须唯一
            if(deviceType == DeviceType.Child) {
                this.getById(entity.getPid()).ifNotPresentThrow("网关子设备必须指定父网关设备").ifPresent(parent -> {
                    entity.setParentDeviceSn(parent.getDeviceSn());
                    if(oldStatus != null && (!parent.getDeviceSn().equals(oldStatus.getParentDeviceSn())
                            || !entity.getProductId().equals(oldStatus.getProductId()))) { // 切换了网关或者产品
                        entity.setStatus(DeviceStatus.offline); // 重置设备状态到离线
                    }

                    this.getByDeviceSn(entity.getDeviceSn(), productDto.getProtocolCode(), parent.getDeviceSn())
                            .ifPresent(item -> {
                                if(entity.getId() == null) { // 新增
                                    throw new ServiceException("网关设备["+parent.getDeviceSn()+"]已包含子设备编号["+ entity.getDeviceSn()+"]");
                                } else if(!item.getId().equals(entity.getId())) { // 更新
                                    throw new ServiceException("网关设备["+parent.getDeviceSn()+"]已包含子设备编号["+ entity.getDeviceSn()+"]");
                                }
                            });
                });
            } else { // 边缘网关设备和直连设备在同一网络组件(协议)下设备编号必须唯一
                this.getByDeviceSn(entity.getDeviceSn(), productDto.getProtocolCode(), null)
                        .ifPresent(item -> {
                            if(entity.getId() == null) { // 新增
                                throw new ServiceException("网络组件["+productDto.getProtocolCode()+"]已包含设备编号["+ entity.getDeviceSn()+"]");
                            } else if(!item.getId().equals(entity.getId())) { // 更新
                                throw new ServiceException("网络组件["+productDto.getProtocolCode()+"]已包含设备编号["+ entity.getDeviceSn()+"]");
                            }
                        });

                // 客户端的网关设备如果产品启用则尝试连接网关
                // 交由客户端自己去连接(2024-06-24)
                if(deviceType == DeviceType.Gateway) {
                    if(oldStatus != null) { // 属于更新操作
                        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(productDto.getProtocolCode());
                        if(supplier instanceof ClientProtocolSupplier) {
                            if(oldStatus.getStatus() == DeviceStatus.online) {
                                // 修改了设备编号
                                if (!oldStatus.getDeviceSn().equals(entity.getDeviceSn())) {
                                    throw new ServiceException("请先断开连接");
                                }

                                // 修改了网关配置
                                if (oldStatus.getConfig() != null) {
                                    oldStatus.getConfig().forEach((key, value) -> {
                                        Object newValue = entity.getConfig().get(key);
                                        if (newValue != null) {
                                            if (value == null) {
                                                throw new ServiceException("请先断开连接");
                                            } else if (!newValue.equals(value)) {
                                                throw new ServiceException("请先断开连接");
                                            }
                                        }
                                    });
                                }
                            } else { // 更新操作直接移除客户端
                                ((ClientProtocolSupplier) supplier).removeClient(oldStatus.getDeviceSn());
                            }
                        }
                    }

                }
            }
        });
    }

    @Override
    @Transactional
    public BooleanResult updateById(Device entity) {
        RealtimeStatus oldCache = this.getDeviceStatusFromCache(entity.getId());
        if(oldCache == null) {
            return BooleanResult.buildFalse("设备不存在");
        }

        validate(entity, oldCache);

        return super.updateById(entity).ifPresentAndTrue(item -> {
            RealtimeStatus newCache = this.getDeviceStatusFromCache(entity.getId());

            // 切换了网关或者切换了产品清除实时数据
            if(!oldCache.getProductId().equals(entity.getProductId()) || // 切换产品
                    (newCache.getType() == DeviceType.Child && !oldCache.getParentDeviceSn().equals(entity.getParentDeviceSn()))) { // 切换网关
                try {
                    realtimeDataService.remove(oldCache.getProtocolCode(), oldCache.buildKey());
                } catch (Exception e) {
                    //
                }
            }

            // 网关设备处理
            if(oldCache.getType() == DeviceType.Gateway) {
                // 网关设备如果修改了设备编号
                if(!oldCache.getDeviceSn().equals(newCache.getDeviceSn())) {
                    Map<String, RealtimeStatus> children = iotCacheManager.listForParent(oldCache.getProtocolCode(), oldCache.getDeviceSn());
                    if(children != null) {
                        // 移除所有旧的设备
                        children.values().forEach(iotCacheManager::removeDevice);

                        // 缓存新的子设备
                        List<RealtimeStatus> childDevices = this.getBaseMapper().listDeviceStatus(newCache.getProtocolCode(), null, newCache.getDeviceSn());
                        if(childDevices != null) {
                            childDevices.forEach(iotCacheManager::cache);
                        }
                    }
                }
            }

            iotCacheManager.update(oldCache, newCache);
        });
    }

    @Override
    public void update(String deviceSn, DeviceStatus status) {
        update(Wrappers.<Device>lambdaUpdate()
                .set(Device::getStatus, status)
                .set(Device::getSwitchTime, new Date())
                .eq(Device::getDeviceSn, deviceSn));
    }

    @Override
    public DetailResult<Device> getByDeviceSn(String deviceSn) {
        return getOne(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceSn, deviceSn));
    }

    @Override
    public CurrentDeviceDto countCurrentDevice() {
        return getBaseMapper().countCurrentDevice();
    }

    @Override
    public DetailResult<DeviceDto> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public ListResult<Device> listOfGateway(Long gatewayId) {
        return new ListResult<>(this.getBaseMapper().listOfGateway(gatewayId));
    }

    @Override
    public ListResult<DeviceDto> listByProductId(Long productId) {
        return new ListResult<>(this.getBaseMapper().listByProductId(productId));
    }

    @Override
    public DetailResult<Device> getByDeviceSn(String deviceSn, String protocolCode, String gatewayDeviceSn) {
        return new DetailResult<>(getBaseMapper().getByDeviceSn(deviceSn, protocolCode, gatewayDeviceSn));
    }

    @Override
    public RealtimeStatus getDeviceStatusFromCache(Long deviceId) {
        return this.getBaseMapper().getDeviceCacheById(deviceId);
    }

    @Override
    public List<RealtimeStatus> listDeviceStatusById(Collection<?> idList) {
        if(CollectionUtil.isEmpty(idList)) {
            return Collections.EMPTY_LIST;
        }

        return this.getBaseMapper().listDeviceStatusById(idList);
    }

    @Override
    public ListResult<RealtimeStatus> listDeviceStatus() {
        return new ListResult<>(this.getBaseMapper().listDeviceCache());
    }

    @Override
    @Transactional
    public void updateDeviceStatus(RealtimeStatus device) {
        Date switchTime = new Date();
        this.update(Wrappers.<Device>lambdaUpdate()
                .set(Device::getStatus, device.getStatus())
                .set(Device::getSwitchTime, switchTime)
                .eq(Device::getUid, device.getUid()));

        // 如果是边缘网关下线则所有的网关子设备也必须下线
        if(device.getType() == DeviceType.Gateway && device.getStatus() == DeviceStatus.offline) {
            Map<String, RealtimeStatus> childDeviceStatus = this.listChildDeviceStatusFromCache(device.getProtocolCode(), device.getDeviceSn());
            if(CollectionUtil.isNotEmpty(childDeviceStatus)) {
                Collection<RealtimeStatus> values = childDeviceStatus.values();
                List<String> uidList = values.stream().map(item -> item.getUid()).collect(Collectors.toList());
                this.update(Wrappers.<Device>lambdaUpdate()
                        .set(Device::getSwitchTime, switchTime)
                        .set(Device::getStatus, DeviceStatus.offline)
                        .in(Device::getUid, uidList));

                values.forEach(item -> item.setStatus(DeviceStatus.offline));
                CollectListenerManager.getInstance().deviceStatusPublish(listener -> listener.supplier(childDeviceStatus.values())); // 推送离线信息到前端
                this.iotCacheManager.updateChildStatus(device.getProtocolCode(), device.getDeviceSn(), DeviceStatus.offline);
            }
        }

        this.iotCacheManager.updateStatus(device.getProtocolCode(), device.buildDeviceKey(), device.getStatus());
        CollectListenerManager.getInstance().deviceStatusPublish(listener -> listener.supplier(device));
    }

    @Override
    public RealtimeStatus getDeviceStatus(String protocolCode, String deviceSn, String parentDeviceSn) {
        return this.getBaseMapper().getDeviceStatus(protocolCode, deviceSn, parentDeviceSn);
    }

    @Override
    public DetailResult<Device> getByUid(String uid) {
        return new DetailResult<>(this.getBaseMapper().getByUid(uid));
    }

    @Override
    public DeviceStatusDto countDeviceStatus(GroupAndProductParamDto param) {
        return getBaseMapper().countDeviceStatus(param);
    }

    @Override
    public List<EMapDeviceDto> listOfEMap(Long productId, Long deviceGroupId) {
        return this.getBaseMapper().listOfEMap(productId, deviceGroupId);
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isEmpty(idList)) {
            return BooleanResult.buildFalse("未指定要移除的记录");
        }

        if(idList.size() > 1) {
            return BooleanResult.buildFalse("不支持批量删除");
        }

        List<RealtimeStatus> statusList = this.listDeviceStatusById(idList);
        if(CollectionUtil.isNotEmpty(statusList)) {
            statusList.forEach(item -> {
                if(item.getType() == DeviceType.Gateway) {
                    this.getOne(Wrappers.<Device>lambdaQuery().eq(Device::getPid, item.getId()).last("limit 1")).ifPresentThrow("请选删除子设备");
                }
            });

            return super.removeByIds(idList).ifPresentAndTrue(item -> {
                statusList.forEach(status -> {
                    if(status.getType() == DeviceType.Child) {
                        iotCacheManager.removeByKey(status.getProtocolCode(), status.getDeviceSn(), status.getParentDeviceSn());
                    } else {
                        if(status.getType() == DeviceType.Gateway) {

                            // 获取此网关设备下的所有网关子设备
                            Map<String, RealtimeStatus> fromCache = this.listChildDeviceStatusFromCache(status.getProtocolCode(), status.getDeviceSn());
                            if(CollectionUtil.isNotEmpty(fromCache)) {
                                // 从数据库移除子设备记录
                                this.remove(Wrappers.<Device>lambdaQuery().eq(Device::getPid, status.getId()));

                                // 从缓存移除所有的子设备
                                fromCache.forEach((key, value) -> {
                                    iotCacheManager.removeByKey(value.getProtocolCode(), value.getDeviceSn(), value.getParentDeviceSn());
                                });
                            }
                        }

                        IotNetworkUtil.removeClient(status);
                        iotCacheManager.removeByKey(status.getProtocolCode(), status.getDeviceSn());
                    }
                });
            });
        } else {
            return BooleanResult.buildTrue("移除成功");
        }
    }
}
