package com.iteaj.framework.spi.iot.consts;

/**
 * 功能类型
 */
public enum FuncType {

    R("读"),
    W("写"),
    RW("读写"),
    ;

    private String desc;

    FuncType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
