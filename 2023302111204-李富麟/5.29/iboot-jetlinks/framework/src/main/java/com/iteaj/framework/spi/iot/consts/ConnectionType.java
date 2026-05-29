package com.iteaj.framework.spi.iot.consts;

/**
 * 连接类型
 */
public enum ConnectionType {
    Client("客户端"), // 客户端
    Server("服务端"), // 服务端
    ;

    private String desc;

    ConnectionType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
