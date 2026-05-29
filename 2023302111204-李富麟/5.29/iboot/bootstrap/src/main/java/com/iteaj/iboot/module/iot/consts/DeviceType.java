package com.iteaj.iboot.module.iot.consts;

import com.iteaj.framework.IVOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DeviceType {

    Direct("设备直连"),
    Gateway("边缘网关"),
    Child("网关子设备"),
    ;

    private String desc;

    DeviceType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static List<IVOption> options() {
        return Arrays.stream(DeviceType.values())
                .map(item -> new IVOption(item.desc, item.name()))
                .collect(Collectors.toList());
    }
}
