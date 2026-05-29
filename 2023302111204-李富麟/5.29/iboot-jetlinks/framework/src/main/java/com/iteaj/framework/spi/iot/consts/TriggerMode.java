package com.iteaj.framework.spi.iot.consts;

/**
 * 接口触发方式
 */
public enum TriggerMode {

    /**
     * 设备主动上报
     */
    active("主动"),

    /**
     * 服务器主动(设备被动)上报
     */
    passive("被动")
    ;

    private String desc;

    TriggerMode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
