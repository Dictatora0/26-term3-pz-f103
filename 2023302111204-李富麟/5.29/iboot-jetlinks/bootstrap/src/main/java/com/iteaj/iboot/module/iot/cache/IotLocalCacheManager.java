package com.iteaj.iboot.module.iot.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IotLocalCacheManager implements IotCacheManager {

    private final Map<String, RealtimeStatus> uidMaps = new HashMap<>(320);
    private final Map<String, Set<RealtimeStatus>> productMaps = new ConcurrentHashMap<>(50);
    private final Map<Long, Set<RealtimeStatus>> groupMaps = new ConcurrentHashMap<>(50);
    // protocolCode -> deviceKey
    private final Map<String, Map<String, RealtimeStatus>> realtimeMap = new ConcurrentHashMap<>(500);

    @Override
    public RealtimeStatus cache(RealtimeStatus device) {
        validateParams(device);
        Map<String, RealtimeStatus> deviceMaps = realtimeMap.get(device.getProtocolCode());
        if(deviceMaps == null) {
            synchronized (this) {
                deviceMaps = realtimeMap.get(device.getProtocolCode());
                if(deviceMaps == null) {
                    realtimeMap.put(device.getProtocolCode(), deviceMaps = new ConcurrentHashMap<>());
                    deviceMaps.put(device.buildDeviceKey(), device);
                    Set<RealtimeStatus> productSets = productMaps.computeIfAbsent(device.getProductCode(), k -> new HashSet<>());
                    productSets.add(device);

                    if(device.getGroupId() != null) {
                        Set<RealtimeStatus> statuses = groupMaps.computeIfAbsent(device.getGroupId(), k -> new HashSet<>());
                        statuses.add(device);
                    }
                }
            }
        } else {
            String deviceKey = device.buildDeviceKey();
            deviceMaps.put(deviceKey, device);
            Set<RealtimeStatus> productSet = productMaps.get(device.getProductCode());
            if(productSet == null) {
                synchronized (this) {
                    productSet = productMaps.computeIfAbsent(device.getProductCode(), k -> new HashSet<>());
                }
            }
            productSet.add(device);

            if(device.getGroupId() != null) {
                Set<RealtimeStatus> statuses = groupMaps.get(device.getGroupId());
                if(statuses == null) {
                    synchronized (this) {
                        statuses = groupMaps.computeIfAbsent(device.getGroupId(), k -> new HashSet<>());
                    }
                }

                statuses.add(device);
            }
        }

        uidMaps.put(device.getUid(), device);
        return device;
    }

    @Override
    public RealtimeStatus update(RealtimeStatus oldValue, RealtimeStatus newValue) {
        RealtimeStatus realtimeStatus = removeDevice(oldValue);
        this.cache(newValue);
        return realtimeStatus;
    }

    private void validateParams(RealtimeStatus device) {
        if(null == device) {
            throw new IllegalArgumentException("参数错误");
        }
        if(device.getType() == null) {
            throw new IllegalArgumentException("设备类型[type]必填");
        }
        if(device.getUid() == null) {
            throw new IllegalArgumentException("设备[uid]必填");
        }
        if(StrUtil.isBlank(device.getDeviceSn())) {
            throw new IllegalArgumentException("设备编号[deviceSn]必填");
        }
        if(StrUtil.isBlank(device.getProductCode())) {
            throw new IllegalArgumentException("产品编号[productCode]必填");
        }
        if(StrUtil.isBlank(device.getProtocolCode())) {
            throw new IllegalArgumentException("协议码[protocolCode]必填");
        }
    }

    @Override
    public Set<RealtimeStatus> removeByGroupId(Long groupId) {
        if(groupId == null) {
            throw new IllegalArgumentException("参数错误");
        }

        return groupMaps.remove(groupId);
    }

    @Override
    public RealtimeStatus removeDevice(RealtimeStatus device) {
        if(null == device) {
            throw new IllegalArgumentException("参数错误");
        }
        if(StrUtil.isBlank(device.getDeviceSn())) {
            throw new IllegalArgumentException("设备编号[deviceSn]必填");
        }
        if(StrUtil.isBlank(device.getProtocolCode())) {
            throw new IllegalArgumentException("协议码[protocolCode]必填");
        }

        if(StrUtil.isBlank(device.getParentDeviceSn())) {
            return this.removeByKey(device.getProtocolCode(), device.getDeviceSn());
        } else {
            return this.removeByKey(device.getProtocolCode(), device.getDeviceSn(), device.getParentDeviceSn());
        }
    }

    @Override
    public RealtimeStatus removeByKey(String protocolCode, String deviceSn) {
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            return removeRealtimeStatus(statusMap.remove(deviceSn));
        }

        return null;
    }

    @Override
    public RealtimeStatus removeByKey(String protocolCode, String deviceSn, String parentDeviceSn) {
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            return removeRealtimeStatus(statusMap.remove(RealtimeStatus.buildDeviceKey(deviceSn, parentDeviceSn)));
        }

        return null;
    }

    private RealtimeStatus removeRealtimeStatus(RealtimeStatus remove) {
        if(remove != null) {
            // 从组里面移除
            if(remove.getGroupId() != null) {
                Set<RealtimeStatus> statuses = groupMaps.get(remove.getGroupId());
                if(statuses != null) {
                    statuses.remove(remove);
                }
            }

            // 从产品里面移除
            Set<RealtimeStatus> realtimeStatuses = productMaps.get(remove.getProductCode());
            if(CollectionUtil.isNotEmpty(realtimeStatuses)) {
                realtimeStatuses.remove(remove);
            }

            uidMaps.remove(remove.getUid());
        }

        return remove;
    }

    @Override
    public RealtimeStatus updateStatus(String protocolCode, DeviceKey key, DeviceStatus status) {
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            RealtimeStatus realtimeStatus = statusMap.get(key.getKey());
            if(realtimeStatus != null) {
                realtimeStatus.setStatus(status);
                realtimeStatus.setUpdateTime(new Date());
            }

            return realtimeStatus;
        }
        return null;
    }

    @Override
    public RealtimeStatus updateStatus(String protocolCode, String deviceSn, DeviceStatus status) {
        return updateStatus(protocolCode, DeviceKey.build(deviceSn, null), status);
    }

    @Override
    public RealtimeStatus updateStatus(String protocolCode, String deviceSn, String parentDeviceSn, DeviceStatus status) {
        return this.updateStatus(protocolCode, DeviceKey.build(deviceSn, parentDeviceSn), status);
    }

    @Override
    public RealtimeStatus getByUid(String uid) {
        return uidMaps.get(uid);
    }

    @Override
    public RealtimeStatus get(String protocolCode, String deviceSn) {
        if(StrUtil.isBlank(protocolCode)) {
            throw new IllegalArgumentException("参数[protocolCode]必填");
        }
        if(StrUtil.isBlank(deviceSn)) {
            throw new IllegalArgumentException("参数[deviceSn]必填");
        }
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            return statusMap.get(deviceSn);
        }

        return null;
    }

    @Override
    public RealtimeStatus get(String protocolCode, DeviceKey key) {
        if(key == null) {
            throw new IllegalArgumentException("参数[key]必填");
        }
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            return statusMap.get(key.getKey());
        }

        return null;
    }

    @Override
    public RealtimeStatus get(String protocolCode, String deviceSn, String parentDeviceSn) {
        if(StrUtil.isBlank(protocolCode)) {
            throw new IllegalArgumentException("参数[protocolCode]必填");
        }
        if(StrUtil.isBlank(deviceSn)) {
            throw new IllegalArgumentException("参数[deviceSn]必填");
        }
        if(StrUtil.isBlank(parentDeviceSn)) {
            throw new IllegalArgumentException("参数[parentDeviceSn]必填");
        }

        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            return statusMap.get(RealtimeStatus.buildDeviceKey(deviceSn, parentDeviceSn));
        }

        return null;
    }

    @Override
    public Map<String, RealtimeStatus> list(String protocolCode, String... deviceSn) {
        if(StrUtil.isBlank(protocolCode)) {
            throw new IllegalArgumentException("参数[protocolCode]必填");
        }

        if(ArrayUtil.isEmpty(deviceSn)) {
            throw new IllegalArgumentException("未指定参数[deviceSn[]]");
        }

        Map<String, RealtimeStatus> result = new HashMap<>();
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            for (int i = 0; i < deviceSn.length; i++) {
                String key = deviceSn[i];
                result.put(key, statusMap.get(key));
            }
        }

        return result;
    }

    @Override
    public Map<String, RealtimeStatus> listForParent(String protocolCode, String parentDeviceSn, String... deviceSn) {
        if(StrUtil.isBlank(protocolCode)) {
            throw new IllegalArgumentException("参数[protocolCode]必填");
        }
        if(StrUtil.isBlank(parentDeviceSn)) {
            throw new IllegalArgumentException("未指定参数[parentDeviceSn]");
        }

        Map<String, RealtimeStatus> result = new HashMap<>();
        Map<String, RealtimeStatus> statusMap = realtimeMap.get(protocolCode);
        if(statusMap != null) {
            if(ArrayUtil.isNotEmpty(deviceSn)) {
                for (int i = 0; i < deviceSn.length; i++) {
                    String key = deviceSn[i];
                    result.put(key, statusMap.get(RealtimeStatus.buildDeviceKey(key, parentDeviceSn)));
                }
            } else {
                statusMap.forEach((key, device) -> {
                    if(device.isChild(parentDeviceSn)) {
                        result.put(device.getDeviceSn(), device);
                    }
                });
            }
        }

        return result;
    }

    @Override
    public Set<RealtimeStatus> listByGroupId(Long groupId) {
        if(groupId == null) {
            throw new IllegalArgumentException("未指定参数[groupId]");
        }

        Set<RealtimeStatus> statusSet = groupMaps.get(groupId);
        return statusSet != null ? statusSet : Collections.emptySet();
    }

    @Override
    public Collection<RealtimeStatus> listByProductCode(String productCode) {
        if(StrUtil.isBlank(productCode)) {
            throw new IllegalArgumentException("未指定参数[productCode]");
        }

        Set<RealtimeStatus> statusSet = productMaps.get(productCode);
        return statusSet != null ? statusSet : Collections.emptySet();
    }

    @Override
    public Collection<RealtimeStatus> listGroupAndProduct(Long groupId, String productCode) {
        if(groupId == null) {
            throw new IllegalArgumentException("未指定参数[groupId]");
        }
        if(StrUtil.isBlank(productCode)) {
            throw new IllegalArgumentException("未指定参数[productCode]");
        }

        Set<RealtimeStatus> realtimeStatuses = groupMaps.get(groupId);
        if(realtimeStatuses != null) {
            return realtimeStatuses.stream()
                    .filter(item -> Objects.equals(productCode, item.getProductCode()))
                    .collect(Collectors.toList());
        }

        return Collections.emptySet();
    }

    @Override
    public void updateChildStatus(String protocolCode, String parentDeviceSn, DeviceStatus status) {
        this.listForParent(protocolCode, parentDeviceSn).values().forEach(item -> {
            item.setStatus(status).setUpdateTime(new Date());
        });
    }

}
