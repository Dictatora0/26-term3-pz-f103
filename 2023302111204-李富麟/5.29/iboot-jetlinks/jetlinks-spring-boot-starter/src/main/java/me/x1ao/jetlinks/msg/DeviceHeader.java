package me.x1ao.jetlinks.msg;

import lombok.Data;

@Data
public class DeviceHeader {
    private String deviceName;
    private String productName;
    private String productId;
    private Boolean async;
    private String traceparent;
}
