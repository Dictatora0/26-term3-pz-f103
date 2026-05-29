package com.iteaj.framework.spi.iot.consts;

/**
 * 网关连接类型
 */
public enum GatewayType {

    /**
     * 设备直连
     */
    Direct("设备直连"),

    /**
     * 连接网关
     */
    Gateway("边缘网关")
    ;

    private String desc;

    GatewayType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
