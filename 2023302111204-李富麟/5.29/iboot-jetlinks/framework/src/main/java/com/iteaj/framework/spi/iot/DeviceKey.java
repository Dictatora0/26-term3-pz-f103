package com.iteaj.framework.spi.iot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceKey {

    private String key;

    private String deviceSn;

    private String parentDeviceSn;

    public static final String DEVICE_SPILT = "->";
    public static final String DEVICE_SN_KEY = "deviceSn";
    public static final String PARENT_DEVICE_SN_KEY = "parentDeviceSn";

    protected DeviceKey(String deviceSn, String parentDeviceSn) {
        this.deviceSn = deviceSn;
        this.parentDeviceSn = parentDeviceSn;
        this.key = parentDeviceSn != null ? (parentDeviceSn + DEVICE_SPILT + deviceSn) : deviceSn;
    }

    public static DeviceKey build(String deviceSn, String parentDeviceSn) {
        return new DeviceKey(deviceSn, parentDeviceSn);
    }

    @Override
    public String toString() {
        return getKey();
    }
}
