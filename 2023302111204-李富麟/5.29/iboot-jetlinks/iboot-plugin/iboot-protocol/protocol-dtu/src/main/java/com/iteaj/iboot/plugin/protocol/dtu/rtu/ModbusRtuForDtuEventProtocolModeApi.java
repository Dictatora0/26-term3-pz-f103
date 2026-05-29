package com.iteaj.iboot.plugin.protocol.dtu.rtu;

import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusEventProtocolModelApi;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuCommonProtocol;

public class ModbusRtuForDtuEventProtocolModeApi extends AbstractModbusEventProtocolModelApi {

    public ModbusRtuForDtuEventProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    public ModbusCommonProtocol buildRead01(String equipCode, int device, int start, int num) {
        return ModbusRtuForDtuCommonProtocol.buildRead01(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead02(String equipCode, int device, int start, int num) {
        return ModbusRtuForDtuCommonProtocol.buildRead02(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead03(String equipCode, int device, int start, int num) {
        return ModbusRtuForDtuCommonProtocol.buildRead03(equipCode, device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead04(String equipCode, int device, int start, int num) {
        return ModbusRtuForDtuCommonProtocol.buildRead04(equipCode, device, start, num);
    }
}
