package com.iteaj.framework.spi.iot.consts;

/**
 * api配置方法
 */
public enum ApiConfigDirection {

    UP("上行"),
    DOWN("下行")
    ;

    private String desc;

    ApiConfigDirection(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
