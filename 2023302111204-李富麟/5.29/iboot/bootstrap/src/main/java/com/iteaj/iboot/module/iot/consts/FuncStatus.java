package com.iteaj.iboot.module.iot.consts;

public enum FuncStatus {

    enabled("启用"), // 启用
    disabled("停用") // 禁用
    ;

    private String desc;

    FuncStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
