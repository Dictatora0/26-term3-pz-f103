package com.iteaj.iboot.module.iot.consts;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备类型别名
 */
public enum DeviceTypeAlias {
    PLC("plc"),
    DTU("dtu"),
    MQTT("mqtt"),
    MODBUS("modbus"),
    GATEWAY("网关"),
    ;

    private String desc;

    DeviceTypeAlias(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static List<TypeAliasOptions> options() {
        return Arrays.stream(DeviceTypeAlias.values())
                .map(item -> new TypeAliasOptions(item.desc, item.name()))
                .collect(Collectors.toList());
    }
}
