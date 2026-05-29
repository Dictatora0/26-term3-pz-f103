package com.iteaj.plugin.protocol.mqtt.impl;

import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.protocol.AbstractEventProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttSubscribeProtocol;

import java.util.function.Consumer;

public class DefaultMqttSubscribeProtocolModelApi extends AbstractEventProtocolModelApi {

    public DefaultMqttSubscribeProtocolModelApi(String code, String name) {
        super(code, name);
    }

    @Override
    protected Protocol buildProtocol(ProtocolModelApiInvokeParam arg) {
        return new DefaultMqttSubscribeProtocol(null);
    }

    @Override
    protected void doInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) {

    }
}
