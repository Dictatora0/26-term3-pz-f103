package com.iteaj.framework.spi.iot.consts;

/**
 * 功能类型
 */
public enum AttrType {

    R("读"),
    W("写"),
    RW("读写"),
    IGNORE("忽略"),
    STATUS("响应状态"),
    ;

    private String desc;

    AttrType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
