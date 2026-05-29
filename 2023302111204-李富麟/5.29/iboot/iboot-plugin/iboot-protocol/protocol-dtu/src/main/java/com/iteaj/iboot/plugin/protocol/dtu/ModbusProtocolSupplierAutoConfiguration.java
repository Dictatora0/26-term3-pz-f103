package com.iteaj.iboot.plugin.protocol.dtu;

import com.iteaj.iboot.plugin.protocol.dtu.rtu.ModbusRtuForDtuProtocolSupplier;
import com.iteaj.iboot.plugin.protocol.dtu.tcp.ModbusTcpForDtuProtocolSupplier;
import org.springframework.context.annotation.Bean;

public class ModbusProtocolSupplierAutoConfiguration {

    @Bean
    public ModbusTcpForDtuProtocolSupplier modbusTcpForDtuProtocolSupplier() {
        return new ModbusTcpForDtuProtocolSupplier();
    }

    @Bean
    public ModbusRtuForDtuProtocolSupplier modbusRtuForDtuProtocolSupplier() {
        return new ModbusRtuForDtuProtocolSupplier();
    }
}
