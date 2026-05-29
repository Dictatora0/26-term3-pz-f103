package com.iteaj.iboot.plugin.protocol.modbus;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.NetworkConfigImpl;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.StaticClientProtocolSupplier;
import com.iteaj.framework.spi.iot.consts.*;
import com.iteaj.framework.spi.iot.protocol.*;
import com.iteaj.iot.AbstractProtocol;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.ClientComponent;
import com.iteaj.iot.client.IotClient;
import com.iteaj.iot.modbus.consts.ModbusCode;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModbusProtocolSupplier<N extends NetworkConfigImpl, C extends ClientComponent, K extends IotClient> extends StaticClientProtocolSupplier<N, C, K> {

    private DefaultProtocolModel protocolModel;
    public static final String dataType = "dataType";
    private static final String numRemark = "String和Hex类型必填(一个寄存器两个字节)";
    private static List<ProtocolModelApiConfigOption> readOptions = new ArrayList<>();
    private static List<ProtocolModelApiConfigOption> writeOptions = new ArrayList<>();

    private ParamMeta[] deviceConfig = new ParamMeta[] {
            ParamMeta.buildRequiredSelect("dataFormat", "数据格式", "ABCD", ""
                    , new IVOption("ABCD", "ABCD")
                    , new IVOption("BADC", "BADC")
                    , new IVOption("CDAB", "CDAB")
                    , new IVOption("DCBA", "DCBA")),
    };
    static {
        readOptions.add(new ProtocolModelApiConfigOption("readShort", "short", "占1个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readUShort", "ushort", "占1个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readInt", "int", "占2个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readUInt", "uint", "占2个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readLong", "long", "占4个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readFloat", "float", "占2个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readDouble", "double", "占4个寄存器"));
        readOptions.add(new ProtocolModelApiConfigOption("readHex", "hex", "自定义数量"));
        readOptions.add(new ProtocolModelApiConfigOption("readString", "string", "自定义数量"));
        readOptions.add(new ProtocolModelApiConfigOption("readTimestamp", "timestamp", "占4个寄存器(long)"));

        writeOptions.add(new ProtocolModelApiConfigOption("writeShort", "short", "占1个寄存器"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeInt", "int", "占2个寄存器"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeLong", "long", "占4个寄存器"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeFloat", "float", "占2个寄存器"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeDouble", "double", "占4个寄存器"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeHex", "hex", "按具体值"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeString", "string", "按具体值"));
        writeOptions.add(new ProtocolModelApiConfigOption("writeTimestamp", "timestamp", "占4个寄存器(long)"));
    }

    public AbstractModbusProtocolSupplier(String code, TransportProtocol protocol, String remark) {
        protocolModel = new DefaultProtocolModel(code, CtrlMode.POINT, protocol, remark)
                .addModelAttr(new ProtocolModelAttr(dataType, "数据类型", DataType.t_string, AttrType.IGNORE,"要操作的数据类型"))
                .addModelAttr(new ProtocolModelAttr(PointProtocolConfig.POINT_NUMBER, "寄存器数量", DataType.t_int, AttrType.IGNORE, numRemark))
                .addModelAttr(new ProtocolModelAttr(PointProtocolConfig.POINT_ADDRESS, "寄存器地址", DataType.t_string, AttrType.IGNORE, "寄存器地址"))
                .addModelAttr(new ProtocolModelAttr(PointProtocolConfig.POINT_VALUE, "点位值", DataType.t_any, AttrType.IGNORE,"读/写寄存器的值(跟随数据类型转换)"))
                .addModelApi(createEventApi(ModbusCode.Read01.name(), "读线圈(01)", "读线圈")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addUpConfig(ProtocolModelApiConfig.upBuild(PointProtocolConfig.POINT_VALUE, "读取到的值")))
                .addModelApi(createEventApi(ModbusCode.Read02.name(), "读离散量输入(02)", "读离散量输入")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addUpConfig(ProtocolModelApiConfig.upBuild(PointProtocolConfig.POINT_VALUE, "读取到的值")))
                .addModelApi(createEventApi(ModbusCode.Read03.name(), "读保持寄存器(03)", "读保持寄存器")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(dataType, "读取值的数据类型").setOptions(readOptions))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_NUMBER, numRemark))
                        .addUpConfig(ProtocolModelApiConfig.upBuild(PointProtocolConfig.POINT_VALUE, "读取到的值")))
                .addModelApi(createEventApi(ModbusCode.Read04.name(), "读输入寄存器(04)", "读输入寄存器")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(dataType, "读取值的数据类型").setOptions(readOptions))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_NUMBER, numRemark))
                        .addUpConfig(ProtocolModelApiConfig.upBuild(PointProtocolConfig.POINT_VALUE, "读取到的值")))
                .addModelApi(createFuncApi(ModbusCode.Write05.name(), "写单个线圈(05)", "写单个线圈")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_VALUE, "要写的值")))
                .addModelApi(createFuncApi(ModbusCode.Write06.name(), "写单个保持寄存器(06)", "写单个保持寄存器")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_VALUE, "要写的值")))
                .addModelApi(createFuncApi(ModbusCode.Write0F.name(), "写多个线圈(0F)", "写多个线圈")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_VALUE, "要写的值")))
                .addModelApi(createFuncApi(ModbusCode.Write10.name(), "写多个保持寄存器(10)", "写多个保持寄存器")
                        .addDownConfig(ProtocolModelApiConfig.downBuild(dataType, "写入值的数据类型").setOptions(writeOptions))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_ADDRESS, ModbusProtocolConsts.MODBUS_POSITION))
                        .addDownConfig(ProtocolModelApiConfig.downBuild(PointProtocolConfig.POINT_VALUE, "要写的值")));
    }

    protected abstract AbstractModbusFuncProtocolModelApi createFuncApi(String code, String name, String remark);

    protected abstract AbstractModbusEventProtocolModelApi createEventApi(String code, String name, String remark);

    @Override
    public DataSupplier getDataSupplier(Protocol protocol, ProtocolModelApiInvokeParam param, Object value) {
        DataSupplier supplier = new DataSupplier(getProtocol().getCode(), param.buildKey());
        if(CollectionUtil.isNotEmpty(param.listUpAttrs())) {
            ModbusCode code = protocol.protocolType();
            AbstractProtocol abstractProtocol = (AbstractProtocol) protocol;
            if(code.getCode() <= 0x04) { // 读指令
                CollectStatus status = CollectStatus.from(abstractProtocol.getExecStatus());
                param.listUpAttrs().forEach(item -> {
                    String address = param.getString(PointProtocolConfig.POINT_ADDRESS);
                    if(status == CollectStatus.Success) {
                        Object resolverValue = dataResolver(item, value);
                        supplier.addValue(item.getId(), address, item.getField(), resolverValue);
                    } else {
                        supplier.addValue(item.getId(), address, item.getField(), status);
                    }
                });
            }
        }

        return supplier;
    }

    @Override
    public ProtocolModel getProtocol() {
        return this.protocolModel;
    }

    public ParamMeta[] getDeviceConfig() {
        return deviceConfig;
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.Gateway;
    }

    @Override
    public ProtocolImplMode getImplMode() {
        return ProtocolImplMode.Internal;
    }

}
