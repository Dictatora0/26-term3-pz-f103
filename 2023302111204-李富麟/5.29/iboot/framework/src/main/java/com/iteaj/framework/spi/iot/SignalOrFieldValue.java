package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.CollectStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class SignalOrFieldValue {

    /**
     * 属性或信号id
     */
    private Long id;

    /**
     * 值
     */
    private Object value;

    /**
     * 点位地址
     */
    private String address;

    /**
     * 采集时间
     */
    private Date collectTime;

    /**
     * 点位或者字段
     * 值必须在同一个{@link DeviceKey}下唯一
     */
    private String signalOrField;

    /**
     * 采集状态
     */
    private CollectStatus status;

    public SignalOrFieldValue(Long id, String signalOrField, Date collectTime, Object value) {
        this.id = id;
        this.value = value;
        this.collectTime = collectTime;
        this.signalOrField = signalOrField;
        this.status = CollectStatus.Success;
    }

    public SignalOrFieldValue(Long id, String signalOrField, Date collectTime, CollectStatus status) {
        this.id = id;
        this.status = status;
        this.collectTime = collectTime;
        this.signalOrField = signalOrField;
    }

    public SignalOrFieldValue builder(CollectStatus status) {
        this.status = status; return this;
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, Object value) {
        return build(id, signalOrField, new Date(), value);
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, CollectStatus status) {
        return new SignalOrFieldValue(id, signalOrField, new Date(), status);
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, Date collectTime, Object value) {
        return new SignalOrFieldValue(id, signalOrField, collectTime, value);
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, Date collectTime, CollectStatus status) {
        return new SignalOrFieldValue(id, signalOrField, collectTime, status);
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, Date collectTime, Object value, String address) {
        return new SignalOrFieldValue(id, signalOrField, collectTime, value).setAddress(address);
    }

    public static SignalOrFieldValue build(Long id, String signalOrField, Date collectTime, CollectStatus status, String address) {
        return new SignalOrFieldValue(id, signalOrField, collectTime, status).setAddress(address);
    }
}
