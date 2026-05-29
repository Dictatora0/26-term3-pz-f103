package com.iteaj.framework.spi.iot.consts;

/**
 * 点位协议配置
 */
public interface PointProtocolConfig {

    /**
     * 点位数量
     */
    String POINT_NUMBER = "num";

    /**
     * 点位地址
     */
    String POINT_ADDRESS = "address";

    /**
     * modbus子设备编号
     */
    String POINT_CHILD_SN = "childSn";

    /**
     * 点位值
     */
    String POINT_VALUE = "value";

    /**
     * 点位字段
     */
    String POINT_FIELD_KEY = "POINT_FIELD_";
}
