package com.iteaj.iboot.plugin.protocol.dtu.rtu;

import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractDtuForModbusProtocolSupplier;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusEventProtocolModelApi;
import com.iteaj.iboot.plugin.protocol.dtu.AbstractModbusFuncProtocolModelApi;
import com.iteaj.iot.config.ConnectProperties;
import com.iteaj.iot.modbus.server.dtu.ModbusRtuForDtuServerComponent;

public class ModbusRtuForDtuProtocolSupplier extends AbstractDtuForModbusProtocolSupplier<ModbusRtuForDtuServerComponent> {

    public ModbusRtuForDtuProtocolSupplier() {
        super(ProtocolCodes.DTU_MODBUS_RTU.getValue());
    }

    @Override
    public String getDesc() {
        return "基于DTU网关的Modbus Rtu协议提供";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ModbusRtuForDtuServerComponent doCreateComponent(DtuNetworkConfig config) {
        ConnectProperties connectProperties = new ConnectProperties(config.getHost(), config.getPort());
        if(config.getAllIdle() != null && config.getAllIdle() > 0) {
            connectProperties.setAllIdleTime(config.getAllIdle());
        } else if(config.getReadIdle() != null && config.getReadIdle() > 0) {
            connectProperties.setReaderIdleTime(config.getReadIdle());
        } else if(config.getWriteIdle() != null && config.getWriteIdle() > 0) {
            connectProperties.setWriterIdleTime(config.getWriteIdle());
        }

        return new ModbusRtuForDtuServerComponent(connectProperties, config.getMessageType());
    }

    @Override
    protected AbstractModbusFuncProtocolModelApi createFuncApi(String code, String name, String remark) {
        return new ModbusRtuForDtuFuncProtocolModelApi(code, name, remark);
    }

    @Override
    protected AbstractModbusEventProtocolModelApi createEventApi(String code, String name, String remark) {
        return new ModbusRtuForDtuEventProtocolModeApi(code, name, remark);
    }
}
