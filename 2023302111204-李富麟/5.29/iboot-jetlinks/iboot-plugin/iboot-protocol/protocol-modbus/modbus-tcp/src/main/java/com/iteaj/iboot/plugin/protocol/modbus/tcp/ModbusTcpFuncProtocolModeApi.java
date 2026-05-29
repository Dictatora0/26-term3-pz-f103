package com.iteaj.iboot.plugin.protocol.modbus.tcp;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.FrameworkManager;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.IotClient;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientProtocol;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientCommonProtocol;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientMessage;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuCommonProtocol;

public class ModbusTcpFuncProtocolModeApi extends AbstractModbusFuncProtocolModelApi {

    public ModbusTcpFuncProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    protected ModbusCommonProtocol buildWrite05(int device, int start, ModbusCoilStatus status) {
        return ModbusTcpClientCommonProtocol.buildWrite05(device, start, status);
    }

    @Override
    protected ModbusCommonProtocol buildWrite06(int device, int start, short value) {
        return ModbusTcpClientCommonProtocol.buildWrite06(device, start, value);
    }

    @Override
    protected ModbusCommonProtocol buildWrite0F(int device, int start, byte[] write) {
        return ModbusTcpClientCommonProtocol.buildWrite0F(device, start, write);
    }

    @Override
    protected ModbusCommonProtocol buildWrite10(int device, int start, DataFormat format, Object value) {
        if(value instanceof byte[]) {
            byte[] message = (byte[]) value;
            int num = message.length % 2 == 0 ? message.length / 2 : message.length / 2 + 1;
            return ModbusTcpClientCommonProtocol.buildWrite10(device, start, num, message);
        }

        return ModbusTcpClientCommonProtocol.buildWrite10(device, start, format, value);
    }

    @Override
    protected InvokeResult doProtocolInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg) {
        IotClient client = FrameworkManager.getClient(arg.getParentDeviceSn(), ModbusTcpClientMessage.class);
        if(client != null) {
            Integer timeout = arg.getParentDeviceConfig("timeout");
            if(timeout == null || timeout <= 0) {
                throw new ProtocolInvokeException("请求超时时间必须大于0["+timeout+"]");
            }

            ((ModbusTcpClientCommonProtocol)protocol).sync(timeout * 1000).request(client.getConfig());
            return InvokeResult.status(((ModbusTcpClientCommonProtocol) protocol).getExecStatus(), protocol);
        } else {
            throw new ProtocolInvokeException("网关设备不存在["+arg.getParentDeviceSn()+"]");
        }
    }
}
