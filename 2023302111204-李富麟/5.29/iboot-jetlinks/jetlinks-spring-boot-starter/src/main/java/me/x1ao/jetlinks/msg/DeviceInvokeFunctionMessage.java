package me.x1ao.jetlinks.msg;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceInvokeFunctionMessage extends DeviceMsg {
    private String functionId;
    private List<DeviceFunctionInput> inputs;

}
