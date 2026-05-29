package com.iteaj.iboot.plugin.protocol.modbus.tcp;

import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.NetworkConfigImpl;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusEventProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.modbus.AbstractModbusProtocolSupplier;
import com.iteaj.iot.client.ClientConnectProperties;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClient;
import com.iteaj.iot.modbus.client.tcp.ModbusTcpClientComponent;
import org.springframework.stereotype.Component;

/**
 * modbus tcp协议提供
 */
@Component
public class ModbusTcpProtocolSupplier extends AbstractModbusProtocolSupplier<NetworkConfigImpl.TcpNetworkConfig, ModbusTcpClientComponent, ModbusTcpClient> {

    private ParamMeta[] GatewayMetas = new ParamMeta[]{
            ParamMeta.buildRequired("host", "主机"),
            ParamMeta.buildRequiredNumber("port", "端口", 502, ""),
            ParamMeta.buildRequiredNumber("timeout", "请求超时(秒)", 5, "请求超时时间"),
            ParamMeta.buildNumber("readIdle", "读空闲(秒)", 0, "小于等于0则不启用"),
    };

    public ModbusTcpProtocolSupplier() {
        super("MODBUS_TCP", TransportProtocol.TCP, "用于提供边缘网关Modbus Tcp协议的支持");
    }

    @Override
    public String getDesc() {
        return "Modbus Tcp协议提供";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ModbusTcpClientComponent doCreateComponent(NetworkConfigImpl.TcpNetworkConfig config) {
        return new ModbusTcpClientComponent();
    }

    @Override
    protected AbstractModbusFuncProtocolModelApi createFuncApi(String code, String name, String remark) {
        return new ModbusTcpFuncProtocolModeApi(code, name, remark);
    }

    @Override
    protected AbstractModbusEventProtocolModelApi createEventApi(String code, String name, String remark) {
        return new ModbusTcpEventProtocolModeApi(code, name, remark);
    }

    @Override
    public ParamMeta[] getGatewayConfig() {
        return GatewayMetas;
    }

    @Override
    protected ModbusTcpClient doCreateClient(NetworkConfigImpl.TcpNetworkConfig config, ModbusTcpClientComponent component) {
        ClientConnectProperties connectProperties = new ClientConnectProperties(config.getHost(), config.getPort());
        if(config.getReadIdle() != null && config.getReadIdle() > 0) {
            connectProperties.setReaderIdleTime(config.getReadIdle());
        }

        connectProperties.setConnectKey(config.getDeviceSn());
        return (ModbusTcpClient) component.createNewClient(connectProperties);
    }
}
