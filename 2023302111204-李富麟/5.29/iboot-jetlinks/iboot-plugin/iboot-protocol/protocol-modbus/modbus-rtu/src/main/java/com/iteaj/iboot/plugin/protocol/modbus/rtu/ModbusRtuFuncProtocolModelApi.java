package com.iteaj.iboot.plugin.protocol.modbus.rtu;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.FrameworkManager;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.IotClient;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientMessage;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientProtocol;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;

public class ModbusRtuFuncProtocolModelApi extends AbstractModbusFuncProtocolModelApi {

    public ModbusRtuFuncProtocolModelApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    protected ModbusCommonProtocol buildWrite05(int device, int start, ModbusCoilStatus status) {
        return ModbusRtuClientProtocol.buildWrite05(device, start, status);
    }

    @Override
    protected ModbusCommonProtocol buildWrite06(int device, int start, short value) {
        return ModbusRtuClientProtocol.buildWrite06(device, start, value);
    }

    @Override
    protected ModbusCommonProtocol buildWrite0F(int device, int start, byte[] write) {
        return ModbusRtuClientProtocol.buildWrite0F(device, start, write);
    }

    @Override
    protected ModbusCommonProtocol buildWrite10(int device, int start, DataFormat format, Object value) {
        if(value instanceof byte[]) {
            byte[] message = (byte[]) value;
            int num = message.length % 2 == 0 ? message.length / 2 : message.length / 2 + 1;
            return ModbusRtuClientProtocol.buildWrite10(device, start, num, message);
        }

        return ModbusRtuClientProtocol.buildWrite10(device, start, format, value);
    }

    @Override
    protected InvokeResult doProtocolInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg) {
        String com = arg.getParentDeviceSn();
        IotClient client = FrameworkManager.getClient(com, ModbusRtuClientMessage.class);
        if(client != null) {
            ((ModbusRtuClientProtocol)protocol).request(com);
            return InvokeResult.status(((ModbusRtuClientProtocol) protocol).getExecStatus(), protocol);
        } else {
            throw new ProtocolInvokeException("网关设备不存在[" + arg.getParentDeviceSn() + "]");
        }
    }
}
