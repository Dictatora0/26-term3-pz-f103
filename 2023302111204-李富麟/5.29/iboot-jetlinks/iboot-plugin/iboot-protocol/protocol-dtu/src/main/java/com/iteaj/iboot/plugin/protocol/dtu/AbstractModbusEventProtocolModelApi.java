package com.iteaj.iboot.plugin.protocol.dtu;

import cn.hutool.core.util.StrUtil;
import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.PointProtocolConfig;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.framework.spi.iot.protocol.AbstractEventProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.ProtocolException;
import com.iteaj.iot.consts.ExecStatus;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.Payload;
import com.iteaj.iot.modbus.RealCoilPayload;
import com.iteaj.iot.modbus.consts.ModbusBitStatus;
import com.iteaj.iot.modbus.consts.ModbusCode;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuCommonProtocol;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuCommonProtocol;
import com.iteaj.iot.modbus.server.rtu.ModbusRtuBody;
import com.iteaj.iot.modbus.server.tcp.ModbusTcpBody;
import com.iteaj.iot.utils.ByteUtil;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractModbusEventProtocolModelApi extends AbstractEventProtocolModelApi {

    public AbstractModbusEventProtocolModelApi(String code, String name) {
        this(code, name, null);
    }

    public AbstractModbusEventProtocolModelApi(String code, String name, String remark) {
        super(code, name, remark, TriggerMode.passive);
    }

    @Override
    protected Protocol buildProtocol(ProtocolModelApiInvokeParam arg) {
        ModbusCode modbusCode = ModbusCode.valueOf(getCode());
        String dataType = arg.getString(AbstractDtuForModbusProtocolSupplier.dataType);

        int start;
        try {
            start = arg.getInteger(PointProtocolConfig.POINT_ADDRESS);
        } catch (Exception e) {
            throw new ProtocolInvokeException("寄存器地址必须是[0-65535]的数字");
        }

        int childSn;
        try {
            childSn = Integer.valueOf(arg.getDeviceSn());
        } catch (NumberFormatException e) {
            throw new ProtocolInvokeException("从机设备地址必须是[0-255]的数字");
        }

        /**
         * 调用子设备 -> 设备编号用父网关设备编号
         * 调用网关设备 -> 设备编号为网关设备编号
         */
        String deviceSn = StrUtil.isNotBlank(arg.getParentDeviceSn()) ? arg.getParentDeviceSn() : arg.getDeviceSn();

        int num = 1;
        ModbusCommonProtocol protocol;
        switch (modbusCode) {
            case Read01:
                protocol = this.buildRead01(deviceSn, childSn, start, num);
                break;
            case Read02:
                protocol = this.buildRead02(deviceSn, childSn, start, num);
                break;
            case Read03:
                if(StrUtil.isBlank(dataType)) {
                    throw new ProtocolInvokeException("未找到数据类型参数");
                }

                num = getNum(arg, dataType);
                protocol = this.buildRead03(deviceSn, childSn, start, num);
                break;
            case Read04:
                if(StrUtil.isBlank(dataType)) {
                    throw new ProtocolInvokeException("未找到数据类型参数");
                }

                num = getNum(arg, dataType);
                protocol = this.buildRead04(deviceSn, childSn, start, num);
                break;
            default:
                throw new ProtocolInvokeException(getProtocolModel().getCode(), getCode(), "不支持的功能代码["+getCode()+"]");
        }

        return protocol;
    }

    public abstract ModbusCommonProtocol buildRead01(String equipCode, int device, int start, int num);
    public abstract ModbusCommonProtocol buildRead02(String equipCode, int device, int start, int num);
    public abstract ModbusCommonProtocol buildRead03(String equipCode, int device, int start, int num);
    public abstract ModbusCommonProtocol buildRead04(String equipCode, int device, int start, int num);

    private int getNum(ProtocolModelApiInvokeParam arg, String dataType) {
        switch (dataType) {
            case "short":
            case "ushort":
                return 1;
            case "int":
            case "uint":
            case "float":
                return 2;
            case "long":
            case "double":
            case "timestamp":
                return 4;
            case "string":
            case "hex":
                try {
                    return arg.getInteger(PointProtocolConfig.POINT_NUMBER);
                } catch (Exception e) {
                    throw new ProtocolInvokeException("未指定要读取的寄存器数量");
                }
            default:
                throw new ProtocolInvokeException("不支持的数据类型["+dataType+"]");
        }
    }

    @Override
    protected void doInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) {
        ExecStatus execStatus;
        InvokeResult invokeResult = null;
        DataFormat dataFormat = DataFormat.valueOf(arg.getDeviceConfig("dataFormat"));
        try {
            if(protocol instanceof ModbusTcpForDtuCommonProtocol) {
                ModbusTcpForDtuCommonProtocol commonProtocol = (ModbusTcpForDtuCommonProtocol) protocol;
                commonProtocol.timeout(arg.getTimeout()).request();
                execStatus = commonProtocol.getExecStatus();
                if(execStatus == ExecStatus.success) {
                    ModbusTcpBody body = commonProtocol.responseMessage().getBody();
                    if(body.isSuccess()) {
                        Payload payload = commonProtocol.getPayload(dataFormat);
                        Object value = getValue(payload, arg);
                        invokeResult = InvokeResult.status(execStatus, value, protocol);
                    } else {
                        invokeResult = InvokeResult.fail(body.getErrCode().getDesc(), protocol);
                    }
                } else {
                    invokeResult = InvokeResult.status(execStatus, protocol);
                }
            } else if(protocol instanceof ModbusRtuForDtuCommonProtocol) {
                ModbusRtuForDtuCommonProtocol commonProtocol = (ModbusRtuForDtuCommonProtocol) protocol;
                commonProtocol.timeout(arg.getTimeout()).request();
                execStatus = commonProtocol.getExecStatus();
                if(execStatus == ExecStatus.success) {
                    ModbusRtuBody body = commonProtocol.responseMessage().getBody();
                    if(body.isSuccess()) {
                        Payload payload = commonProtocol.getPayload(dataFormat);
                        Object value = getValue(payload, arg);
                        invokeResult = InvokeResult.status(execStatus, value, protocol);
                    } else {
                        invokeResult = InvokeResult.fail(body.getErrCode().getDesc(), protocol);
                    }
                } else {
                    invokeResult = InvokeResult.status(execStatus, protocol);
                }
            }

            result.accept(invokeResult);
        } catch (ProtocolException e) {
            e.printStackTrace();
            result.accept(InvokeResult.fail(e.getMessage(), protocol));
        } catch (Exception e) {
            e.printStackTrace();
            result.accept(InvokeResult.fail("未知错误["+e.getMessage()+"]", protocol));
        }
    }

    public Object getValue(Payload payload, ProtocolModelApiInvokeParam arg) {
        if(payload instanceof RealCoilPayload) {
            List<ModbusBitStatus> statuses = ((RealCoilPayload) payload).getStatuses();
            return statuses.get(0) == ModbusBitStatus.ON ? Boolean.TRUE : Boolean.FALSE;
        }

        int start = arg.getInteger(PointProtocolConfig.POINT_ADDRESS);
        String dataType = arg.getString(AbstractDtuForModbusProtocolSupplier.dataType);
        switch (dataType) {
            case "short":
                return payload.readShort(start);
            case "ushort":
                return payload.readUShort(start);
            case "int":
                return payload.readInt(start);
            case "uint":
                return payload.readUInt(start);
            case "float":
                return payload.readFloat(start);
            case "long":
                return payload.readLong(start);
            case "double":
                return payload.readDouble(start);
            case "timestamp":
                return new Date(payload.readLong(start));
            case "string":
                int num = arg.getInteger(PointProtocolConfig.POINT_NUMBER);
                return payload.readString(start, num);
            case "hex":
                    return ByteUtil.bytesToHex(payload.getPayload());
            default:
                throw new ProtocolInvokeException("不支持的数据类型["+dataType+"]");
        }
    }
}
