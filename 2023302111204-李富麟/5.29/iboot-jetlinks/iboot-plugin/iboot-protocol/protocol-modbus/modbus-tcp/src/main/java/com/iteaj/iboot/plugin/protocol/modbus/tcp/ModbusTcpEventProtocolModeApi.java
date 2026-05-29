package com.iteaj.iboot.plugin.protocol.modbus.tcp;

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
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientCommonProtocol;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientMessage;
import com.iteaj.iot.modbus.server.tcp.ModbusTcpBody;

public class ModbusTcpEventProtocolModeApi extends AbstractModbusEventProtocolModelApi {

    public ModbusTcpEventProtocolModeApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    public ModbusCommonProtocol buildRead01(int device, int start, int num) {
        return ModbusTcpClientCommonProtocol.buildRead01(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead02(int device, int start, int num) {
        return ModbusTcpClientCommonProtocol.buildRead02(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead03(int device, int start, int num) {
        return ModbusTcpClientCommonProtocol.buildRead03(device, start, num);
    }

    @Override
    public ModbusCommonProtocol buildRead04(int device, int start, int num) {
        return ModbusTcpClientCommonProtocol.buildRead04(device, start, num);
    }

    @Override
    protected InvokeResult doProtocolInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, DataFormat dataFormat) {
        IotClient client = FrameworkManager.getClient(arg.getParentDeviceSn(), ModbusTcpClientMessage.class);
        if(client != null) {
            Integer timeout = arg.getParentDeviceConfig("timeout");
            if(timeout == null || timeout <= 0) {
                throw new ProtocolInvokeException("请求超时时间必须大于0["+timeout+"]");
            }

            ModbusTcpClientCommonProtocol commonProtocol = (ModbusTcpClientCommonProtocol) protocol;
            commonProtocol.sync(timeout * 1000).request(client.getConfig());
            ExecStatus execStatus = commonProtocol.getExecStatus();
            if(execStatus == ExecStatus.success) {
                ModbusTcpBody body = commonProtocol.responseMessage().getBody();
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
            throw new ProtocolInvokeException("网关设备不存在["+arg.getParentDeviceSn()+"]");
        }
    }
}
