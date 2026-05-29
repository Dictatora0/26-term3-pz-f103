package com.iteaj.framework.spi.iot;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.framework.spi.iot.consts.CollectStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DataSupplier {

    /**
     * 设备key信息
     */
    private DeviceKey key;

    /**
     * 采集时间
     */
    private Date collectTime;

    /**
     * 协议码
     */
    private String protocolCode;

    /**
     * 点位或者字段列表值
     */
    private List<SignalOrFieldValue> values;

    public DataSupplier(String protocolCode, DeviceKey key) {
        this.key = key;
        this.collectTime = new Date();
        this.protocolCode = protocolCode;
    }

    public DataSupplier addValue(SignalOrFieldValue value) {
        if(values == null) {
            values = new ArrayList<>();
        }

        values.add(value); return this;
    }

    public DataSupplier addValue(Long id, String signalOrField, Object value) {
        return this.addValue(SignalOrFieldValue.build(id, signalOrField, this.collectTime, value));
    }

    public DataSupplier addValue(Long id, String address, String signalOrField, Object value) {
        return this.addValue(SignalOrFieldValue.build(id, signalOrField, this.collectTime, value, address));
    }

    public DataSupplier addValue(Long id, String address, String signalOrField, CollectStatus status) {
        return this.addValue(SignalOrFieldValue.build(id, signalOrField, this.collectTime, status, address));
    }

    public DataSupplier addValue(Long id, String address, String signalOrField, Object value, Date collectTime) {
        return this.addValue(SignalOrFieldValue.build(id, signalOrField, collectTime, value, address));
    }

    public DataSupplier build(Date collectTime) {
        this.collectTime = collectTime;
        if(CollectionUtil.isNotEmpty(this.getValues())) {
            this.getValues().forEach(item -> item.setCollectTime(collectTime));
        }

        return this;
    }
}
