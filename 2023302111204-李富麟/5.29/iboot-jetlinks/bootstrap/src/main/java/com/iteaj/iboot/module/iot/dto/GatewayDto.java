package com.iteaj.iboot.module.iot.dto;

import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.entity.Gateway;
import lombok.Data;

@Data
public class GatewayDto extends Gateway {

    /**
     * 协议代码
     */
    private String protocolCode;

    /**
     * 协议名称
     */
    private String protocolName;

    /**
     * 协议操控方式
     */
    private CtrlMode ctrlMode;

}
