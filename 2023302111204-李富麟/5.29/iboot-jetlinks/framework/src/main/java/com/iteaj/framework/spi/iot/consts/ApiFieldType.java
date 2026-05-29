package com.iteaj.framework.spi.iot.consts;

public enum ApiFieldType {

    status("状态"),
    field("属性")
    ;

    private String desc;

    ApiFieldType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
