package me.x1ao.jetlinks.msg;

import lombok.Data;

@Data
public class DeviceMsg {
    private String messageId;
    private String messageType;
    private String deviceId;
    private Long timestamp;
    private DeviceHeader headers;

    public String getProductId() {
        return this.headers.getProductId();
    }
}
