package com.iteaj.iboot.plugin.protocol.dtu.tcp;

import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusEventProtocolModelApi;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuCommonProtocol;

public class ModbusTcpForDtuEventProtocolModeApi extends AbstractModbusEventProtocolModelApi {

    public ModbusTcpForDtuEventProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    public ModbusCommonProtocol buildRead01(String equipCode, int device, int start, int num) {
        return ModbusTcpForDtuCommonProtocol.buildRead01(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead02(String equipCode, int device, int start, int num) {
        return ModbusTcpForDtuCommonProtocol.buildRead02(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead03(String equipCode, int device, int start, int num) {
        return ModbusTcpForDtuCommonProtocol.buildRead03(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead04(String equipCode, int device, int start, int num) {
        return ModbusTcpForDtuCommonProtocol.buildRead04(equipCode, device, start, num);
    }
}
