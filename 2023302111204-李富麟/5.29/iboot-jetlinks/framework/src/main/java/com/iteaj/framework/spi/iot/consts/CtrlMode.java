package com.iteaj.framework.spi.iot.consts;

import com.iteaj.framework.IVOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CtrlMode {

    COMMON("常用控制"),
    POINT("点位控制"),
    ;

    private String desc;

    CtrlMode(String desc) {
        this.desc = desc;
    }

    public static List<IVOption> options() {
        return Arrays.stream(CtrlMode.values())
                .map(item -> new IVOption(item.getDesc(), item.name()))
                .collect(Collectors.toList());
    }

    public String getDesc() {
        return desc;
    }
}
