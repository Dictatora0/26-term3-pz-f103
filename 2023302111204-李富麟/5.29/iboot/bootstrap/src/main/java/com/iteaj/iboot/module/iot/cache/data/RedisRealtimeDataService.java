package com.iteaj.iboot.module.iot.cache.data;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.data.ModelAttrRealtimeData;
import com.iteaj.framework.spi.iot.data.RealtimeDeviceStatus;
import com.iteaj.framework.utils.RedisUtils;

import java.util.*;
import java.util.stream.Collectors;

public class RedisRealtimeDataService implements RealtimeDataService{

    private static final String KEY_PREFIX = "Realtime:Data:";

    @Override
    public void remove(String protocolCode) {

    }

    @Override
    public Map<String, RealtimeData> remove(String protocolCode, DeviceKey key) {
        String valueKey = KEY_PREFIX + protocolCode + ":" + key.getKey();
        List<RealtimeData> values = RedisUtils.getOpsForHash().values(valueKey);
        if(CollectionUtil.isNotEmpty(values)) {
            RedisUtils.getTemplate().delete(valueKey);
            return values.stream().collect(Collectors.toMap(RealtimeData::getSignalOrField, item -> item));
        }

        return null;
    }

    @Override
    public RealtimeData remove(String protocolCode, DeviceKey key, String signalOrField) {
        String valueKey = KEY_PREFIX + protocolCode + ":" + key.getKey();
        Object o = RedisUtils.getOpsForHash().get(valueKey, signalOrField);
        if(o instanceof RealtimeData) {
            RedisUtils.getOpsForHash().delete(valueKey, signalOrField);
        }

        return (RealtimeData) o;
    }

    @Override
    public Map<String, RealtimeData> remove(String protocolCode, DeviceKey key, String... signalOrFields) {
        String valueKey = KEY_PREFIX + protocolCode + ":" + key.getKey();
        List<RealtimeData> values = RedisUtils.getOpsForHash().multiGet(valueKey, Arrays.asList(signalOrFields));
        if(CollectionUtil.isNotEmpty(values)) {
            RedisUtils.getOpsForHash().delete(valueKey, signalOrFields);
            return values.stream().collect(Collectors.toMap(RealtimeData::getSignalOrField, item -> item));
        }

        return null;
    }

    @Override
    public void put(String protocolCode, DeviceKey key, SignalOrFieldValue value) {
        String signalOrField = value.getSignalOrField();
        String valueKey = KEY_PREFIX + protocolCode + ":" + key.getKey();
        Object o = RedisUtils.getOpsForHash().get(valueKey, signalOrField);
        if(o instanceof RealtimeData) {
            ((RealtimeData) o).setRealtime(value);
            RedisUtils.getOpsForHash().put(valueKey, signalOrField, o);
        } else {
            RedisUtils.getOpsForHash().put(valueKey
                    , signalOrField, new RealtimeData(value));
        }
    }

    @Override
    public void put(DataSupplier supplier) {
        String valueKey = KEY_PREFIX + supplier.getProtocolCode() + ":" + supplier.getKey().getKey();
        Map<String, SignalOrFieldValue> collect = supplier.getValues().stream()
                .collect(Collectors.toMap(SignalOrFieldValue::getSignalOrField, value -> value));

        Map<String, Object> result = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>(collect.keySet());
        List list = RedisUtils.getOpsForHash().multiGet(valueKey, keys);
        for (int i = 0; i < collect.size(); i++) {
            String key = keys.get(i);
            Object value = list.get(i);
            SignalOrFieldValue fieldValue = collect.get(key);
            if(value instanceof RealtimeData) {
                ((RealtimeData) value).setRealtime(fieldValue);
            } else {
                value = new RealtimeData(fieldValue);
            }

            result.put(key, value);
        }

        RedisUtils.getOpsForHash().putAll(valueKey, result);
    }

    @Override
    public void put(List<DataSupplier> suppliers) {
        suppliers.forEach(item -> {
            this.put(item);
        });
    }

    @Override
    public Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key) {
        Map entries = RedisUtils.getOpsForHash().entries(KEY_PREFIX + protocolCode + ":" + key.getKey());
        return entries;
    }

    @Override
    public RealtimeData getOfDeviceAndKey(String protocolCode, DeviceKey key, String signalOrField) {
        return (RealtimeData) RedisUtils.getOpsForHash().get(KEY_PREFIX + protocolCode + ":" + key.getKey(), signalOrField);
    }

    @Override
    public Map<String, RealtimeData> listOfDevice(String protocolCode, DeviceKey key, String... signalOrFields) {
        List<RealtimeData> list = RedisUtils.getOpsForHash().multiGet(KEY_PREFIX + protocolCode + ":" + key.getKey(), Arrays.asList(signalOrFields));
        if(CollectionUtil.isNotEmpty(list)) {
            return list.stream().collect(Collectors.toMap(RealtimeData::getSignalOrField, item -> item));
        }

        return Collections.EMPTY_MAP;
    }

    @Override
    public ModelAttrRealtimeData getRealtimeData(String uid, String signalOrField) {
        return null;
    }

    @Override
    public RealtimeDeviceStatus getDeviceStatus(String uid) {
        return null;
    }
}
