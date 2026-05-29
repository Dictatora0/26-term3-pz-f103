package com.iteaj.iboot.plugin.protocol.dtu.tcp;

import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractDtuForModbusProtocolSupplier;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusEventProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.config.ConnectProperties;
import com.iteaj.iot.modbus.server.dtu.ModbusTcpForDtuServerComponent;

public class ModbusTcpForDtuProtocolSupplier extends AbstractDtuForModbusProtocolSupplier<ModbusTcpForDtuServerComponent> {

    public ModbusTcpForDtuProtocolSupplier() {
        super(ProtocolCodes.DTU_MODBUS_TCP.getValue());
    }

    @Override
    public String getDesc() {
        return "基于DTU网关的Modbus Tcp协议提供";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ModbusTcpForDtuServerComponent doCreateComponent(DtuNetworkConfig config) {
        ConnectProperties connectProperties = new ConnectProperties(config.getHost(), config.getPort());
        if(config.getAllIdle() != null) {
            connectProperties.setAllIdleTime(config.getAllIdle());
        } else if(config.getReadIdle() != null) {
            connectProperties.setReaderIdleTime(config.getReadIdle());
        } else if(config.getWriteIdle() != null) {
            connectProperties.setWriterIdleTime(config.getWriteIdle());
        }

        return new ModbusTcpForDtuServerComponent(connectProperties, config.getMessageType());
    }

    @Override
    protected AbstractModbusFuncProtocolModelApi createFuncApi(String code, String name, String remark) {
        return new ModbusTcpForDtuFuncProtocolModeApi(code, name, remark);
    }

    @Override
    protected AbstractModbusEventProtocolModelApi createEventApi(String code, String name, String remark) {
        return new ModbusTcpForDtuEventProtocolModeApi(code, name, remark);
    }

}
