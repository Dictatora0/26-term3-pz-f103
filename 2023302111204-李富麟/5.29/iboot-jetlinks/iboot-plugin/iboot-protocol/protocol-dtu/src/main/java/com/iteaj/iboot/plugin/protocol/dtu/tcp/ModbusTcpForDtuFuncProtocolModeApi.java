package com.iteaj.iboot.plugin.protocol.dtu.tcp;

import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuCommonProtocol;

public class ModbusTcpForDtuFuncProtocolModeApi extends AbstractModbusFuncProtocolModelApi {

    public ModbusTcpForDtuFuncProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    protected ModbusCommonProtocol buildWrite05(String equipCode, int device, int start, ModbusCoilStatus status) {
        return ModbusTcpForDtuCommonProtocol.buildWrite05(equipCode, device, start, status);
    }

    @Override
    protected ModbusCommonProtocol buildWrite06(String equipCode, int device, int start, short value) {
        return ModbusTcpForDtuCommonProtocol.buildWrite06(equipCode, device, start, value);
    }

    @Override
    protected ModbusCommonProtocol buildWrite0F(String equipCode, int device, int start, byte[] write) {
        return ModbusTcpForDtuCommonProtocol.buildWrite0F(equipCode, device, start, write);
    }

    @Override
    protected ModbusCommonProtocol buildWrite10(String equipCode, int device, int start, DataFormat format, Object value) {
        if(value instanceof byte[]) {
            byte[] message = (byte[]) value;
            int num = message.length % 2 == 0 ? message.length / 2 : message.length / 2 + 1;
            return ModbusTcpForDtuCommonProtocol.buildWrite10(equipCode, device, start, num, message);
        }

        return ModbusTcpForDtuCommonProtocol.buildWrite10(equipCode, device, start, format, value);
    }
}
