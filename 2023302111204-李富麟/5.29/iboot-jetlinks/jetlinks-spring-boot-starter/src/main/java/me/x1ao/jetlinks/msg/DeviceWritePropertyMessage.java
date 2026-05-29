package me.x1ao.jetlinks.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceWritePropertyMessage extends DeviceMsg {
    private String messageType;
    private Map<String, Object> properties;
}
