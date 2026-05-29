package com.iteaj.iboot.module.iot.cache.data;

import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.data.ModelAttrRealtimeData;
import com.iteaj.framework.spi.iot.data.RealtimeDeviceStatus;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapRealtimeDataService implements RealtimeDataService{

    private final IotCacheManager cacheManager;
    private final Map<String, Map<String, Map<String, RealtimeData>>> realtimeMap = new ConcurrentHashMap<>(500);

    public HashMapRealtimeDataService(IotCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void remove(String protocolCode) {
        realtimeMap.remove(protocolCode);
    }

    @Override
    public Map<String, RealtimeData> remove(String protocolCode, DeviceKey key) {
        Map<String, Map<String, RealtimeData>> productMap = realtimeMap.get(protocolCode);
        if(productMap != null) {
            return productMap.remove(key.getKey());
        }

        return null;
    }

    @Override
    public RealtimeData remove(String protocolCode, DeviceKey key, String signalOrField) {
        Map<String, RealtimeData> realtimeDataMap = remove(protocolCode, key);
        if(null != realtimeDataMap) {
            return realtimeDataMap.remove(signalOrField);
        }

        return null;
    }

    @Override
    public Map<String, RealtimeData> remove(String protocolCode, DeviceKey key, String... signalOrFields) {
        Map<String, RealtimeData> realtimeDataMap = this.listOfDevice(protocolCode, key);
        if(realtimeDataMap != null && signalOrFields != null) {
            Map<String, RealtimeData> result = new HashMap<>();
            Arrays.stream(signalOrFields).forEach(item -> {
                RealtimeData remove = realtimeDataMap.remove(item);
                result.put(item, remove);
            });

            return result;
        }

        return null;
    }

    @Override
    public void put(String protocolCode, DeviceKey key, SignalOrFieldValue value) {
        Map<String, Map<String, RealtimeData>> productMap = realtimeMap.get(protocolCode);
        if(productMap == null) {
            synchronized (this) {
                productMap = realtimeMap.get(protocolCode);
                if(productMap == null) {
                    realtimeMap.put(protocolCode, productMap = new ConcurrentHashMap<>());
                }
            }
        }

        Map<String, RealtimeData> deviceMap = productMap.get(key.getKey());
        if(deviceMap == null) {
            synchronized (this) {
                deviceMap = productMap.get(key.getKey());
                if(deviceMap == null) {
                    productMap.put(key.getKey(), deviceMap = new ConcurrentHashMap<>());
                }
            }
        }

        RealtimeData data = deviceMap.get(value.getSignalOrField());
        if(data == null) {
            deviceMap.put(value.getSignalOrField(), new RealtimeData(value));
        } else {
            data.setRealtime(value);
        }
    }

    @Override
    public void put(DataSupplier supplier) {
        supplier.getValues().forEach(signalOrFieldValue -> {
            this.put(supplier.getProtocolCode(), supplier.getKey(), signalOrFieldValue);
        });
    }

    @Override
    public void put(List<DataSupplier> suppliers) {
        suppliers.forEach(item -> {
            item.getValues().forEach(value -> {
                this.put(item.getProtocolCode(), item.getKey(), value);
            });
        });
    }

    @Override
    public Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key) {
        Map<String, Map<String, RealtimeData>> deviceMap = realtimeMap.get(protocolCode);
        if(deviceMap != null) {
            Map<String, RealtimeData> dataMap = deviceMap.get(key.getKey());
            if(dataMap != null) {
                return dataMap;
            }
        }

        return Collections.EMPTY_MAP;
    }

    @Override
    public RealtimeData getOfDeviceAndKey(String protocolCode, DeviceKey key, String signalOrField) {
        return this.listOfDevice(protocolCode, key).get(signalOrField);
    }

    @Override
    public Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key, String... signalOrFields) {
        Map<String, RealtimeData> realtimeDataMap = listOfDevice(protocolCode, key);
        if(null != realtimeDataMap && signalOrFields != null) {
            Map<String, RealtimeData> result = new HashMap<>();
            Arrays.stream(signalOrFields).forEach(item -> {
                result.put(item, realtimeDataMap.get(item));
            });

            return result;
        }

        return Collections.EMPTY_MAP;
    }

    @Override
    public ModelAttrRealtimeData getRealtimeData(String uid, String signalOrField) {
        RealtimeStatus byUid = cacheManager.getByUid(uid);
        if(byUid != null) {
            return this.getOfDeviceAndKey(byUid.getProtocolCode(), byUid.buildKey(), signalOrField);
        }

        return null;
    }

    @Override
    public RealtimeDeviceStatus getDeviceStatus(String uid) {
        return cacheManager.getByUid(uid);
    }
}
