package com.iteaj.plugin.protocol.mqtt;

import com.iteaj.plugin.protocol.mqtt.impl.DefaultMqttProtocolSupplier;
import org.springframework.context.annotation.Bean;

public class DefaultMqttProtocolSupplierAutoConfiguration {

    @Bean
    public DefaultMqttProtocolSupplier defaultMqttProtocolSupplier() {
        return new DefaultMqttProtocolSupplier();
    }
}
