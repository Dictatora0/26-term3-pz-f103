package com.iteaj.iboot.plugin.protocol.modbus.rtu;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.NetworkConfigImpl;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusEventProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusProtocolSupplier;
import com.iteaj.iot.modbus.client.rtu.ModbusRtuClientComponent;
import com.iteaj.iot.serial.SerialClient;
import com.iteaj.iot.serial.SerialConnectProperties;
import org.springframework.stereotype.Component;

/**
 * modbus rtu协议提供
 */
@Component
public class ModbusRtuProtocolSupplier extends AbstractModbusProtocolSupplier<NetworkConfigImpl.SerialConfig, ModbusRtuClientComponent, SerialClient> {

    private ParamMeta[] GatewayConfig = new ParamMeta[]{
//            ParamMeta.buildRequired("com", "串口", "", "").setPlaceholder("请输入串口地址"),
            ParamMeta.buildRequiredSelect("baudRate", "波特率", "9600", ""
                    , new IVOption("1200", "1200"), new IVOption("2400", "2400")
                    , new IVOption("4800", "4800"), new IVOption("9600", "9600")
                    , new IVOption("14400", "14400"), new IVOption("19200", "19200")
                    , new IVOption("38400", "38400"), new IVOption("56000", "56000")
                    , new IVOption("57600", "57600"), new IVOption("115200", "115200")).setPlaceholder("请输入波特率"),

            ParamMeta.buildRequiredSelect("dataBits", "数据位", "8", ""
                    , new IVOption("5", "5"), new IVOption("6", "6")
                    , new IVOption("7", "7"), new IVOption("8", "8")).setPlaceholder("请输入数据位"),

            ParamMeta.buildRequiredSelect("parity", "校验位", "0", ""
                    , new IVOption("无", "0")
                    , new IVOption("奇校验", "1"), new IVOption("偶校验", "2")
                    , new IVOption("标记校验", "3"), new IVOption("空格校验", "4")).setPlaceholder("请输入校验位"),

            ParamMeta.buildRequiredSelect("stopBits", "停止位", "1", ""
                    , new IVOption("1", "1")
                    , new IVOption("1.5", "2")
                    , new IVOption("2", "3")).setPlaceholder("请输入停止位"),

            ParamMeta.buildRequiredNumber("readIdle", "读超时时间(秒)", 5, "读超时时间"),
    };

    public ModbusRtuProtocolSupplier() {
        super("MODBUS_RTU", TransportProtocol.SERIAL, "用于提供边缘网关Modbus Rtu协议的支持");
    }

    @Override
    public String getDesc() {
        return "Modbus Rtu协议提供";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ModbusRtuClientComponent doCreateComponent(NetworkConfigImpl.SerialConfig config) {
        return new ModbusRtuClientComponent();
    }

    @Override
    public ParamMeta[] getGatewayConfig() {
        return GatewayConfig;
    }

    @Override
    protected AbstractModbusFuncProtocolModelApi createFuncApi(String code, String name, String remark) {
        return new ModbusRtuFuncProtocolModelApi(code, name, remark);
    }

    @Override
    protected AbstractModbusEventProtocolModelApi createEventApi(String code, String name, String remark) {
        return new ModbusRtuEventProtocolModeApi(code, name, remark);
    }

    @Override
    protected SerialClient doCreateClient(NetworkConfigImpl.SerialConfig config, ModbusRtuClientComponent component) {
        SerialConnectProperties connectProperties = new SerialConnectProperties(config.getDeviceSn());
        connectProperties.setBaudRate(config.getBaudRate()).setParity(config.getParity())
                .setDataBits(config.getDataBits()).setStopBits(config.getStopBits());
        return component.createNewClient(connectProperties);
    }
}
