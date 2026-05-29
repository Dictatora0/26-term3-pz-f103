package com.iteaj.iboot.module.iot.cache.entity;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.framework.spi.iot.data.RealtimeDeviceStatus;
import com.iteaj.framework.spi.iot.listener.IotEventPayload;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.beans.Transient;
import java.util.Date;
import java.util.Objects;

import static com.iteaj.framework.spi.iot.DeviceKey.DEVICE_SPILT;

/**
 * 设备实时状态
 */
@Data
@Accessors(chain = true)
public class RealtimeStatus implements RealtimeDeviceStatus, IotEventPayload {

    private Long id;

    /**
     * 设备所属uid
     */
    private String uid;

    /**
     * 设备编号
     * @see com.sun.istack.internal.NotNull
     */
    private String deviceSn;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 父设备编号
     */
    private String parentDeviceSn;

    /**
     * 设备类型
     */
    private DeviceType type;

    /**
     * 所属组
     */
    private Long groupId;

    /**
     * 所属协议
     */
    private String protocolCode;

    /**
     * 所属产品id
     */
    private Long productId;

    /**
     * 所属产品
     */
    private String productCode;

    /**
     * 状态更新时间
     * @see #status
     */
    private Date updateTime;

    /**
     * 设备状态
     */
    private DeviceStatus status;

    /**
     * 设备配置
     */
    private JSONObject config;

    public String buildDeviceKey() {
        if(this.getType() == DeviceType.Child) {
            return parentDeviceSn + DEVICE_SPILT + deviceSn;
        } else {
            return deviceSn;
        }
    }

    public DeviceKey buildKey() {
        return DeviceKey.build(getDeviceSn(), getParentDeviceSn());
    }

    public static String buildDeviceKey(String deviceSn, String parentDeviceSn) {
        return parentDeviceSn + DEVICE_SPILT + deviceSn;
    }

    /**
     * 是否是当前设备的子设备
     * @param parentDeviceSn
     * @return
     */
    public boolean isChild(String parentDeviceSn) {
        return Objects.equals(parentDeviceSn, this.parentDeviceSn);
    }

    /**
     * 是否是当前设备的子设备
     * @param protocolCode 协议码
     * @param parentDeviceSn 父设备编号
     * @return
     */
    public boolean isChild(String protocolCode, String parentDeviceSn) {
        return Objects.equals(protocolCode, this.protocolCode) && Objects.equals(parentDeviceSn, this.parentDeviceSn);
    }

    @Transient
    public JSONObject getConfig() {
        return config;
    }

    public boolean equals(Object o) {
        RealtimeStatus other = (RealtimeStatus) o;
        return Objects.equals(this.getProtocolCode(), other.getProtocolCode())
                && Objects.equals(this.buildDeviceKey(), other.buildDeviceKey());
    }

    @Override
    public String getDeviceStatus() {
        return this.status != null ? this.status.name() : null;
    }

    @Override
    public Long getDeviceGroupId() {
        return this.getGroupId();
    }

    @Override
    public String getDeviceType() {
        return this.type != null ? this.type.name() : null;
    }
}
