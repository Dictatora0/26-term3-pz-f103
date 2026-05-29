package com.iteaj.iboot.plugin.protocol.dtu.rtu;

import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuCommonProtocol;

public class ModbusRtuForDtuFuncProtocolModelApi extends AbstractModbusFuncProtocolModelApi {

    public ModbusRtuForDtuFuncProtocolModelApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    protected ModbusCommonProtocol buildWrite05(String equipCode, int device, int start, ModbusCoilStatus status) {
        return ModbusRtuForDtuCommonProtocol.buildWrite05(equipCode, device, start, status);
    }

    @Override
    protected ModbusCommonProtocol buildWrite06(String equipCode, int device, int start, short value) {
        return ModbusRtuForDtuCommonProtocol.buildWrite06(equipCode, device, start, value);
    }

    @Override
    protected ModbusCommonProtocol buildWrite0F(String equipCode, int device, int start, byte[] write) {
        return ModbusRtuForDtuCommonProtocol.buildWrite0F(equipCode, device, start, write);
    }

    @Override
    protected ModbusCommonProtocol buildWrite10(String equipCode, int device, int start, DataFormat format, Object value) {
        if(value instanceof byte[]) {
            byte[] message = (byte[]) value;
            int num = message.length % 2 == 0 ? message.length / 2 : message.length / 2 + 1;
            return ModbusRtuForDtuCommonProtocol.buildWrite10(equipCode, device, start, num, message);
        }

        return ModbusRtuForDtuCommonProtocol.buildWrite10(equipCode, device, start, format, value);
    }
}
