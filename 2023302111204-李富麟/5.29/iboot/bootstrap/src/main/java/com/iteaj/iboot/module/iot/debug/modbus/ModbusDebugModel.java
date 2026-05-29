package com.iteaj.iboot.module.iot.debug.modbus;

import com.iteaj.iboot.module.iot.debug.DebugModel;
import lombok.Data;

@Data
public class ModbusDebugModel extends DebugModel {

    /**
     * 指令
     */
    private String cmd;

    /**
     * 值类型 int、short、long、float...
     */
    private String type;

    /**
     * 写值 需要和值类型匹配
     */
    private String writeValue;

    /**
     * 寄存器其实地址
     */
    private Integer start;

    /**
     * 从机设备地址
     */
    private String childSn;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 主机
     */
    private String host;

    /**
     * 协议类型(modbus tcp or rtu)
     */
    private String protocolType;

    /**
     * 波特率
     */
    private Integer baudRate;

    /**
     * 数据位
     */
    private Integer dataBits;

    /**
     * 校验位
     */
    private Integer parity;

    /**
     * 停止位
     */
    private Integer stopBits;
}
