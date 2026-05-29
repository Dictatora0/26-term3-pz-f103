package com.iteaj.iboot.module.iot.consts;

import java.io.File;

public interface IotConsts {

    /*采集动作类型*/
    /**
     * 欧姆龙plc
     */
    String COLLECT_ACTION_PLC_OMRON = "PLC:OMRON";

    /**
     * 西门子plc
     */
    String COLLECT_ACTION_PLC_SIEMENS = "PLC:SIEMENS";

    /**
     * 通用dtu协议
     */
    String COLLECT_ACTION_COMMON_DTU = "COMMON:DTU";

    /**
     * modbus rtu for dtu
     */
    String COLLECT_ACTION_MODBUS_DTU_RTU = "MODBUS:DTU:RTU";

    /**
     * modbus tcp for dtu
     */
    String COLLECT_ACTION_MODBUS_DTU_TCP = "MODBUS:DTU:TCP";

    /**
     * modbus tcp for client
     */
    String COLLECT_ACTION_MODBUS_TCP = "CLIENT:MODBUS:TCP";

    /**
     * modbus rtu for client
     */
    String COLLECT_ACTION_MODBUS_RTU = "CLIENT:MODBUS:RTU";

    /**
     * mqtt存储
     */
    String STORE_ACTION_MQTT = "STORE:MQTT";

    /**
     * RDBMS存储
     */
    String STORE_ACTION_RDBMS = "STORE:RDBMS";

    int FIELD_TYPE_BYTE = 1;

    int FIELD_TYPE_SHORT = 2;

    int FIELD_TYPE_INT = 3;

    int FIELD_TYPE_LONG = 4;

    int FIELD_TYPE_FLOAT = 5;

    int FIELD_TYPE_DOUBLE = 6;

    int FIELD_TYPE_BOOLEAN = 7;

    int FIELD_TYPE_STRING = 9;
}
