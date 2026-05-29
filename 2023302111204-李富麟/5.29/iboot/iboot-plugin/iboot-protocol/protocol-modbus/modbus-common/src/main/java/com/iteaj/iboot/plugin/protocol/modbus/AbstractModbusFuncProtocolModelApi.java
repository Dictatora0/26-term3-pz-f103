package com.iteaj.iboot.plugin.protocol.modbus;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.PointProtocolConfig;
import com.iteaj.framework.spi.iot.protocol.AbstractFuncProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.ProtocolException;
import com.iteaj.iot.format.DataFormat;
import com.iteaj.iot.modbus.ModbusCommonProtocol;
import com.iteaj.iot.modbus.consts.ModbusCode;
import com.iteaj.iot.modbus.consts.ModbusCoilStatus;
import com.iteaj.iot.utils.ByteUtil;

import java.util.function.Consumer;

public abstract class AbstractModbusFuncProtocolModelApi extends AbstractFuncProtocolModelApi {

    public AbstractModbusFuncProtocolModelApi(String code, String name) {
        super(code, name);
    }

    public AbstractModbusFuncProtocolModelApi(String code, String name, String remark) {
        super(code, name, remark);
    }

    @Override
    protected Protocol buildProtocol(ProtocolModelApiInvokeParam arg) {
        ModbusCode modbusCode = ModbusCode.valueOf(getCode());
        String value = arg.getString(PointProtocolConfig.POINT_VALUE);
        if(value == null) {
            throw new ProtocolInvokeException("写入的值不能为空");
        }

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

        ModbusCommonProtocol protocol;
        switch (modbusCode) {
            case Write05:
                Boolean aBoolean = arg.getBoolean(PointProtocolConfig.POINT_VALUE);
                ModbusCoilStatus status = Boolean.TRUE.equals(aBoolean) ? ModbusCoilStatus.ON : ModbusCoilStatus.OFF;
                protocol = this.buildWrite05(childSn, start, status);
                break;
            case Write06:
                Short aShort = Short.valueOf(value);
                protocol = this.buildWrite06(childSn, start, aShort);
                break;
            case Write0F:
                protocol = this.buildWrite0F(childSn, start, new byte[]{});
                break;
            case Write10:
                String dataFormat = arg.getDeviceConfig("dataFormat");
                Object writeValue = getValue(arg.getString(AbstractModbusProtocolSupplier.dataType), value);
                protocol = this.buildWrite10(childSn, start, DataFormat.valueOf(dataFormat), writeValue);
                break;
            default:
                throw new ProtocolInvokeException(getProtocolModel().getCode(), getCode(), "不支持的功能代码["+getCode()+"]");
        }

        return protocol;
    }

    protected abstract ModbusCommonProtocol buildWrite05(int device, int start, ModbusCoilStatus status);
    protected abstract ModbusCommonProtocol buildWrite06(int device, int start, short value);
    protected abstract ModbusCommonProtocol buildWrite0F(int device, int start, byte[] write);
    protected abstract ModbusCommonProtocol buildWrite10(int device, int start, DataFormat format, Object value);

    private Object getValue(String dataType, String value) {
        switch (dataType) {
            case "int": return Integer.valueOf(value);
            case "float": return Float.valueOf(value);
            case "double": return Double.valueOf(value);
            case "short": return Short.valueOf(value);
            case "long": return Long.valueOf(value);
            case "hex":
                return ByteUtil.hexToByte(value);
            case "string":
                return value;
            default: throw new IllegalArgumentException("不支持的值类型["+dataType+"]");
        }
    }

    @Override
    protected void doInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) {
        try {
            InvokeResult invokeResult = doProtocolInvoke(protocol, arg);
            result.accept(invokeResult);
        } catch (ProtocolException e) {
            e.printStackTrace();
            result.accept(InvokeResult.fail(e.getMessage(), protocol));
        } catch (Exception e) {
            e.printStackTrace();
            result.accept(InvokeResult.fail("未知错误["+e.getMessage()+"]", protocol));
        }
    }

    protected abstract InvokeResult doProtocolInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg);
}
