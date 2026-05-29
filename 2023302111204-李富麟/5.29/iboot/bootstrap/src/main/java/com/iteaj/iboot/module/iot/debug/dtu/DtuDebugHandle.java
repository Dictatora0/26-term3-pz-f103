package com.iteaj.iboot.module.iot.debug.dtu;

import com.iteaj.framework.result.HttpResult;
import com.iteaj.iboot.module.iot.debug.DebugHandle;
import com.iteaj.iboot.module.iot.debug.DebugResult;
import com.iteaj.iboot.module.iot.debug.DebugWebsocketWrite;
import com.iteaj.iot.Message;
import com.iteaj.iot.ProtocolException;
import com.iteaj.iot.consts.ExecStatus;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.Payload;
import com.iteaj.iot.modbus.ReadPayload;
import com.iteaj.iot.modbus.RealCoilPayload;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuCommonProtocol;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuCommonProtocol;
import com.iteaj.iot.modbus.server.rtu.ModbusRtuBody;
import com.iteaj.iot.modbus.server.tcp.ModbusTcpBody;
import com.iteaj.iot.server.ServerMessage;
import com.iteaj.iot.server.dtu.impl.CommonDtuProtocol;
import com.iteaj.iot.server.protocol.ServerInitiativeProtocol;
import com.iteaj.iot.utils.ByteUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class DtuDebugHandle implements DebugHandle<DtuDebugModel> {

    @Override
    public String type() {
        return "dtu";
    }

    @Override
    public void handle(DtuDebugModel model, DebugWebsocketWrite write) {
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
                    case "cus": // 自定义协议
                        otherHandle(model, write); break;
                    default: write.write(HttpResult.Fail("不支持的协议类型["+protocolType+"]"));
                }
            }
        } catch (Exception e) {
            write.write(HttpResult.Fail(e.getMessage()));
        }
    }

    private void otherHandle(DtuDebugModel model, DebugWebsocketWrite write) {
        String message = model.getMessage();
        if(!StringUtils.hasText(message)) {
            write.write(HttpResult.Fail("未指定要发送的报文")); return;
        }

        String cmd = model.getCmd();
        if(!StringUtils.hasText(cmd)) {
            write.write(HttpResult.Fail("未指定操作指令")); return;
        }

        try {
            byte[] bytes;
            try {
                bytes = ByteUtil.hexToByte(message);
            } catch (Exception e) {
                write.write(HttpResult.Fail("只支持16进制报文格式")); return;
            }

            DebugResult result = new DebugResult(model)
                    .setDeviceSn(model.getDeviceSn())
                    .setReqTime(System.currentTimeMillis())
                    .setReqMsg(ByteUtil.bytesToHexByFormat(bytes));
            byte[] respMsg = new byte[0];
            CommonDtuProtocol protocol = new CommonDtuProtocol(model.getDeviceSn());
            if("read".equals(cmd)) {
                respMsg = protocol.read(bytes);
            } else if("write".equals(cmd)) {
                respMsg = protocol.write(bytes);
            } else if("writeOfAsync".equals(cmd)) {
                protocol.writeOfAsync(bytes);
            } else {
                write.write(HttpResult.Fail("只支持指令[read、write、writeOfAsync]")); return;
            }

            result.setRespTime(System.currentTimeMillis())
                    .setRespMsg(ByteUtil.bytesToHexByFormat(respMsg == null ? new byte[0] : respMsg));
            if(protocol.getExecStatus() != ExecStatus.success) {
                write.write(HttpResult.StatusCode(result, protocol.getExecStatus().desc, 208));
            } else {
                write.write(HttpResult.Success(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
            write.write(HttpResult.Fail(e.getMessage()));
        }
    }

    private void modbusTcpHandle(DtuDebugModel model, DebugWebsocketWrite write) {
        ModbusTcpForDtuCommonProtocol protocol;
        if(StringUtils.hasText(model.getMessage())) {
            try {
                byte[] bytes = ByteUtil.hexToByte(model.getMessage());
                protocol = ModbusTcpForDtuCommonProtocol.build(model.getDeviceSn(), bytes, null);
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
                    protocol = ModbusTcpForDtuCommonProtocol.buildRead01(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "02":
                    protocol = ModbusTcpForDtuCommonProtocol.buildRead02(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "03":
                    protocol = ModbusTcpForDtuCommonProtocol.buildRead03(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "04":
                    protocol = ModbusTcpForDtuCommonProtocol.buildRead04(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "05":
                    Object writeValue = parseWriteValue(model);
                    if(writeValue == null) {
                        write.write(HttpResult.Fail("写05功能码的值只能是[0 或 1]")); return;
                    }
                    protocol = ModbusTcpForDtuCommonProtocol.buildWrite05(model.getDeviceSn()
                            , address, model.getStart(), (ModbusCoilStatus) writeValue); break;
                case "06":
                    protocol = ModbusTcpForDtuCommonProtocol.buildWrite06(model.getDeviceSn()
                            , address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                case "10":
                    byte[] writeBytes = (byte[]) parseWriteValue(model);
                    protocol = ModbusTcpForDtuCommonProtocol.buildWrite10(model.getDeviceSn()
                            , address, model.getStart(), writeBytes.length / 2, writeBytes); break;
                case "0F":
                    protocol = ModbusTcpForDtuCommonProtocol.buildWrite0F(model.getDeviceSn()
                            , address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                default: write.write(HttpResult.Fail("不支持的指令["+cmd+"]"));
                    return;
            }
        }

        sendProtocolRequest(model, write, protocol);
    }

    private boolean modbusValidate(DtuDebugModel model, DebugWebsocketWrite write) {
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

    private void modbusRtuHandle(DtuDebugModel model, DebugWebsocketWrite write) {
        ModbusRtuForDtuCommonProtocol protocol;
        if(StringUtils.hasText(model.getMessage())) {
            try {
                String hexMsg = model.getMessage().replaceAll(" ", "");
                byte[] bytes = ByteUtil.hexToByte(hexMsg);
                protocol = ModbusRtuForDtuCommonProtocol.build(model.getDeviceSn(), bytes, null);
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
                    protocol = ModbusRtuForDtuCommonProtocol.buildRead01(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "02":
                    protocol = ModbusRtuForDtuCommonProtocol.buildRead02(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "03":
                    protocol = ModbusRtuForDtuCommonProtocol.buildRead03(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "04":
                    protocol = ModbusRtuForDtuCommonProtocol.buildRead04(model.getDeviceSn()
                            , address, model.getStart(), this.getTypeCoilNum(model)); break;
                case "05":
                    Object writeValue = parseWriteValue(model);
                    if(writeValue == null) {
                        write.write(HttpResult.Fail("写05功能码的值只能是[0 或 1]")); return;
                    }
                    protocol = ModbusRtuForDtuCommonProtocol.buildWrite05(model.getDeviceSn()
                            , address, model.getStart(), (ModbusCoilStatus) writeValue); break;
                case "06":
                    protocol = ModbusRtuForDtuCommonProtocol.buildWrite06(model.getDeviceSn()
                            , address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                case "10":
                    byte[] writeBytes = (byte[]) parseWriteValue(model);
                    protocol = ModbusRtuForDtuCommonProtocol.buildWrite10(model.getDeviceSn()
                            , address, model.getStart(), writeBytes.length / 2, writeBytes); break;
                case "0F":
                    protocol = ModbusRtuForDtuCommonProtocol.buildWrite0F(model.getDeviceSn()
                            , address, model.getStart(), (byte[]) parseWriteValue(model)); break;
                default: write.write(HttpResult.Fail("不支持的指令["+cmd+"]"));
                    return;
            }
        }

        sendProtocolRequest(model, write, protocol);
    }

    private int getTypeCoilNum(DtuDebugModel model) {
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
    private void sendProtocolRequest(DtuDebugModel model, DebugWebsocketWrite write, ServerInitiativeProtocol protocol) {
        try {
            DebugResult result = new DebugResult(model).setReqTime(System.currentTimeMillis());
            protocol.request(protocol1 -> {
                result.setDeviceSn(model.getDeviceSn());
                result.setRespTime(System.currentTimeMillis());

                if(protocol.requestMessage().getMessage() != null) {
                    result.setReqMsg(ByteUtil.bytesToHexByFormat(protocol.requestMessage().getMessage()));
                }

                ServerMessage serverMessage = protocol.responseMessage();
                if(serverMessage != null) {
                    result.setRespMsg(ByteUtil.bytesToHexByFormat(serverMessage.getMessage()));
                }

                if(protocol.getExecStatus() == ExecStatus.success) {
                    Message.MessageBody body = serverMessage.getBody();
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

    private Object parseReadValue(DtuDebugModel model, Payload payload) {
        String type = model.getType();
        switch (type) {
            case "int": return payload.readInt(model.getStart());
            case "float": return payload.readFloat(model.getStart());
            case "double": return payload.readDouble(model.getStart());
            case "short": return payload.readShort(model.getStart());
            case "long": return payload.readLong(model.getStart());
            case "boolean":
                return model.getWriteValue().equals("0") ? ModbusCoilStatus.OFF :
                        model.getWriteValue().equals("1") ? ModbusCoilStatus.ON : null;
            case "string":
                int length = Integer.valueOf(model.getWriteValue());
                return payload.readString(model.getStart(), length);
            default: throw new IllegalArgumentException("不支持的值类型["+type+"]");
        }
    }

    private Object parseWriteValue(DtuDebugModel model) {
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
