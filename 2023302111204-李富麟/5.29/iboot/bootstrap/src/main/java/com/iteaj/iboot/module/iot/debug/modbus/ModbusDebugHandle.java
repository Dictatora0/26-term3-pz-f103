package com.iteaj.iboot.module.iot.debug.modbus;

import com.iteaj.framework.result.HttpResult;
import com.iteaj.iboot.module.iot.debug.DebugHandle;
import com.iteaj.iboot.module.iot.debug.DebugResult;
import com.iteaj.iboot.module.iot.debug.DebugWebsocketWrite;
import com.iteaj.iot.Message;
import com.iteaj.iot.ProtocolException;
import com.iteaj.iot.client.ClientConnectProperties;
import com.iteaj.iot.client.ClientMessage;
import com.iteaj.iot.client.protocol.ClientInitiativeProtocol;
import com.iteaj.iot.consts.ExecStatus;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.Payload;
import com.iteaj.iot.modbus.ReadPayload;
import com.iteaj.iot.modbus.RealCoilPayload;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientProtocol;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientCommonProtocol;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.modbus.server.rtu.ModbusRtuBody;
import com.iteaj.iot.modbus.server.tcp.ModbusTcpBody;
import com.iteaj.iot.modbus.server.tcp.ModbusTcpHeader;
import com.iteaj.iot.serial.SerialConnectProperties;
import com.iteaj.iot.utils.ByteUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class ModbusDebugHandle implements DebugHandle<ModbusDebugModel> {

    @Override
    public String type() {
        return "modbus";
    }

    @Override
    public void handle(ModbusDebugModel model, DebugWebsocketWrite write) {
        String deviceSn = model.getDeviceSn();
        if(!StringUtils.hasText(deviceSn)) {
            write.write(HttpResult.Fail("未指定设备编号[deviceSn]")); return;
        }

        try {
            String protocolType = model.getProtocolType();
            if(!StringUtils.hasText(protocolType)) {
                write.write(HttpResult.Fail("请指定协议类型[protocolType]"));
            } else {
                switch (protocolType) {
                    case "tcp": // modbus tcp
                        modbusTcpHandle(model, write); break;
                    case "rtu": // modbus rtu
                        modbusRtuHandle(model, write); break;
                    default: write.write(HttpResult.Fail("不支持的协议类型["+protocolType+"]"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            write.write(HttpResult.Fail(e.getMessage()));
        }
    }

    private void modbusTcpHandle(ModbusDebugModel model, DebugWebsocketWrite write) {
        ModbusTcpClientCommonProtocol protocol;
        if(StringUtils.hasText(model.getMessage())) {
            try {
                String hexMsg = model.getMessage().replaceAll(" ", "");
                byte[] bytes = ByteUtil.hexToByte(hexMsg);
                ModbusTcpBody empty = ModbusTcpBody.empty();
                ModbusTcpHeader header = ModbusTcpHeader.buildRequestHeader(null, null, null);
                header.setMessage(bytes);
                protocol = ModbusTcpClientCommonProtocol.build(header, empty);
            } catch (Exception e) {
                write.write(HttpResult.Fail("报文只支持十六进制格式")); return;
            }
        } else {
            if(!modbusValidate(model, write)) {
                return;
            }

            String cmd = model.getCmd();
            Integer address = Integer.valueOf(model.getChildSn());
            switch (cmd) {
                case "01":
                    protocol = ModbusTcpClientCommonProtocol.buildRead01(
                            address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "02":
                    protocol = ModbusTcpClientCommonProtocol.buildRead02(
                            address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "03":
                    protocol = ModbusTcpClientCommonProtocol.buildRead03(
                            address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "04":
                    protocol = ModbusTcpClientCommonProtocol.buildRead04(
                            address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "05":
                    Object writeValue = parseWriteValue(model);
                    if(writeValue == null) {
                        write.write(HttpResult.Fail("写05功能码的值只能是[0 或 1]")); return;
                    }
                    protocol = ModbusTcpClientCommonProtocol.buildWrite05(
                            address, model.getStart(), (ModbusCoilStatus) writeValue); break;
                case "06":
                    protocol = ModbusTcpClientCommonProtocol.buildWrite06(
                            address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                case "10":
                    byte[] writeBytes = (byte[]) parseWriteValue(model);
                    protocol = ModbusTcpClientCommonProtocol.buildWrite10(
                            address, model.getStart(), writeBytes.length / 2, writeBytes); break;
                case "0F":
                    protocol = ModbusTcpClientCommonProtocol.buildWrite0F(
                            address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                default: write.write(HttpResult.Fail("不支持的指令["+cmd+"]"));
                    return;
            }
        }

        ClientConnectProperties properties = new ClientConnectProperties(model.getHost(), model.getPort());
        protocol.setClientKey(properties);
        sendProtocolRequest(model, write, protocol);
    }

    private boolean modbusValidate(ModbusDebugModel model, DebugWebsocketWrite write) {
        if(model.getStart() == null) {
            write.write(HttpResult.Fail("未指定寄存器起始地址")); return false;
        }

        if(!StringUtils.hasText(model.getCmd())) {
            write.write(HttpResult.Fail("未指定功能码指令")); return false;
        }

        if(!StringUtils.hasText(model.getType())) {
            write.write(HttpResult.Fail("未指定值类型")); return false;
        }

        return true;
    }

    private void modbusRtuHandle(ModbusDebugModel model, DebugWebsocketWrite write) {
        ModbusRtuClientProtocol protocol = null;
        if(StringUtils.hasText(model.getMessage())) {
            try {
                try {
                    String hexMsg = model.getMessage().replaceAll(" ", "");
                    byte[] bytes = ByteUtil.hexToByte(hexMsg);
                    protocol = ModbusRtuClientProtocol.build(bytes);
                } catch (Exception e) {
                    write.write(HttpResult.Fail("报文只支持十六进制格式")); return;
                }
            } catch (Exception e) {
                write.write(HttpResult.Fail("报文只支持十六进制格式")); return;
            }
        } else {
            if(!modbusValidate(model, write)) {
                return;
            }

            if(true) {
                write.write(HttpResult.Fail("暂不支持modbus rtu调试")); return;
            }

            String cmd = model.getCmd();
            Integer address = Integer.valueOf(model.getChildSn());
            switch (cmd) {
                case "01":
                    protocol = ModbusRtuClientProtocol.buildRead01(address
                            , model.getStart(), this.getTypeCoilNum(model)); break;
                case "02":
                    protocol = ModbusRtuClientProtocol.buildRead02(address
                            , model.getStart(), this.getTypeCoilNum(model)); break;
                case "03":
                    protocol = ModbusRtuClientProtocol.buildRead03(address
                            , model.getStart(), this.getTypeCoilNum(model)); break;
                case "04":
                    protocol = ModbusRtuClientProtocol.buildRead04(address
                            , model.getStart(), this.getTypeCoilNum(model)); break;
                case "05":
                    Object writeValue = parseWriteValue(model);
                    if(writeValue == null) {
                        write.write(HttpResult.Fail("写05功能码的值只能是[0 或 1]")); return;
                    }
                    protocol = ModbusRtuClientProtocol.buildWrite05(address
                            , model.getStart(), (ModbusCoilStatus) writeValue); break;
                case "06":
                    protocol = ModbusRtuClientProtocol.buildWrite06(address
                            , model.getStart(), (byte[]) parseWriteValue(model)); break;
                case "10":
                    byte[] writeBytes = (byte[]) parseWriteValue(model);
                    protocol = ModbusRtuClientProtocol.buildWrite10(address
                            , model.getStart(), writeBytes.length / 2, writeBytes); break;
                case "0F":
                    protocol = ModbusRtuClientProtocol.buildWrite0F(
                            address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                default: write.write(HttpResult.Fail("不支持的指令["+cmd+"]"));
                    return;
            }
        }

        SerialConnectProperties config = new SerialConnectProperties(model.getDeviceSn(), model.getBaudRate())
                .config(model.getDataBits(), model.getStopBits(), model.getParity());
//        protocol.setClientKey(config);
//        sendProtocolRequest(model, write, protocol);
    }

    private int getTypeCoilNum(ModbusDebugModel model) {
        String type = model.getType();
        switch (type) {
            case "int":
            case "float":
                return 2;
            case "double":
            case "long":
                return 4;
            case "short":
            case "boolean":
                return 1;
            case "string":
                String writeValue = model.getWriteValue();
                if(!StringUtils.hasText(writeValue)) {
                    throw new IllegalArgumentException("未指定要读取字符串的长度");
                }
                try {
                    return Integer.valueOf(writeValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("字符串的长度值["+writeValue+"]不支持");
                }
            default: throw new IllegalArgumentException("不支持的值类型["+type+"]");
        }
    }

    /**
     * 发送modbus协议
     * @param model
     * @param write
     * @param protocol
     */
    private void sendProtocolRequest(ModbusDebugModel model, DebugWebsocketWrite write, ClientInitiativeProtocol protocol) {
        try {
            DebugResult result = new DebugResult(model).setReqTime(System.currentTimeMillis());

            protocol.request(protocol1 -> {
                result.setDeviceSn(model.getDeviceSn());
                result.setRespTime(System.currentTimeMillis());

                if(protocol.requestMessage().getMessage() != null) {
                    result.setReqMsg(ByteUtil.bytesToHexByFormat(protocol.requestMessage().getMessage()));
                }

                ClientMessage clientMessage = protocol.responseMessage();
                if(clientMessage != null) {
                    result.setRespMsg(ByteUtil.bytesToHexByFormat(clientMessage.getMessage()));
                }

                if(protocol.getExecStatus() == ExecStatus.success) {
                    Message.MessageBody body = clientMessage.getBody();
                    if(body instanceof ModbusRtuBody) {
                        if(!((ModbusRtuBody) body).isSuccess()) {
                            String reason = ((ModbusRtuBody) body).getErrCode().getDesc();
                            write.write(HttpResult.StatusCode(result, reason, 208));
                            return null;
                        }
                    } else if(body instanceof ModbusTcpBody) {
                        if(!((ModbusTcpBody) body).isSuccess()) {
                            String reason = ((ModbusTcpBody) body).getErrCode().getDesc();
                            write.write(HttpResult.StatusCode(result, reason, 208));
                            return null;
                        }
                    }

                    Payload payload = ((ModbusCommonProtocol) protocol).getPayload();
                    if(payload instanceof ReadPayload) {
                        result.setValue(this.parseReadValue(model, payload).toString());
                    } else if(payload instanceof RealCoilPayload) {
                        result.setValue(payload.readStatus(0).getBit());
                    } else {
                        result.setValue(model.getWriteValue());
                    }
                    write.write(HttpResult.Success(result));
                } else if(protocol.getExecStatus() == ExecStatus.offline) {
                    write.write(HttpResult.StatusCode(result, protocol.getExecStatus().desc, 208));
                } else {
                    write.write(HttpResult.StatusCode(result, protocol.getExecStatus().desc, 208));
                }

                return null;
            });
        } catch (ProtocolException e) {
            e.printStackTrace();
            write.write(HttpResult.Fail(e.getMessage()));
        }
    }

    private Object parseReadValue(ModbusDebugModel model, Payload payload) {
        String type = model.getType();
        switch (type) {
            case "int": return payload.readInt(model.getStart());
            case "float": return payload.readFloat(model.getStart());
            case "double": return payload.readDouble(model.getStart());
            case "short": return payload.readShort(model.getStart());
            case "long": return payload.readLong(model.getStart());
            case "string":
                int length = Integer.valueOf(model.getWriteValue());
                return payload.readString(model.getStart(), length);
            default: throw new IllegalArgumentException("不支持的值类型["+type+"]");
        }
    }

    private Object parseWriteValue(ModbusDebugModel model) {
        String type = model.getType();
        if(!StringUtils.hasText(model.getWriteValue())) {
            throw new IllegalArgumentException("为指定要写的值");
        }

        try {
            switch (type) {
                case "int": return ByteUtil.getBytes(Integer.valueOf(model.getWriteValue()));
                case "float": return ByteUtil.getBytes(Float.valueOf(model.getWriteValue()));
                case "double": return ByteUtil.getBytes(Double.valueOf(model.getWriteValue()));
                case "short": return ByteUtil.getBytesOfReverse(Short.valueOf(model.getWriteValue()));
                case "long": return ByteUtil.getBytes(Long.valueOf(model.getWriteValue()));
                case "boolean":
                    return model.getWriteValue().equals("0") ? ModbusCoilStatus.OFF :
                            model.getWriteValue().equals("1") ? ModbusCoilStatus.ON : null;
                case "string":
                    return model.getWriteValue().getBytes(StandardCharsets.UTF_8);
                default: throw new IllegalArgumentException("不支持的值类型["+type+"]");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("值类型["+type+"]和值["+model.getWriteValue()+"]不匹配");
        }
    }


}
