package com.iteaj.iboot.plugin.protocol.modbus.rtu;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusEventProtocolModelApi;
import com.iteaj.iot.FrameworkManager;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.IotClient;
import com.iteaj.iot.consts.ExecStatus;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.Payload;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientMessage;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientProtocol;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuCommonProtocol;
import com.iteaj.iot.modbus.server.rtu.ModbusRtuBody;

public class ModbusRtuEventProtocolModeApi extends AbstractModbusEventProtocolModelApi {

    public ModbusRtuEventProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    public ModbusCommonProtocol buildRead01(int device, int start, int num) {
        return ModbusRtuClientProtocol.buildRead01(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead02(int device, int start, int num) {
        return ModbusRtuClientProtocol.buildRead02(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead03(int device, int start, int num) {
        return ModbusRtuClientProtocol.buildRead03(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead04(int device, int start, int num) {
        return ModbusRtuClientProtocol.buildRead04(device, start, num);
    }

    @Override
    protected InvokeResult doProtocolInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, DataFormat dataFormat) {
        String com = arg.getParentDeviceSn();
        IotClient client = FrameworkManager.getClient(com, ModbusRtuClientMessage.class);
        if(client != null) {
            ModbusRtuClientProtocol commonProtocol = (ModbusRtuClientProtocol) protocol;
            commonProtocol.request(com);
            ExecStatus execStatus = commonProtocol.getExecStatus();
            if(execStatus == ExecStatus.success) {
                ModbusRtuBody body = commonProtocol.responseMessage().getBody();
                if(body.isSuccess()) {
                    Payload payload = commonProtocol.getPayload(dataFormat);
                    Object value = getValue(payload, arg);
                    return InvokeResult.status(execStatus, value, protocol);
                } else {
                    return InvokeResult.fail(body.getErrCode().getDesc(), protocol);
                }
            } else {
                return InvokeResult.status(execStatus, protocol);
            }
        } else {
            throw new ProtocolInvokeException("网关设备不存在[" + com + "]");
        }
    }
}
