package com.iteaj.plugin.protocol.mqtt.impl;

import cn.hutool.core.text.StrFormatter;
import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.protocol.AbstractFuncProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iot.FrameworkManager;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.IotClient;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttMessage;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttPublishProtocol;
import com.iteaj.iot.consts.ExecStatus;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class DefaultMqttPublishProtocolModelApi extends AbstractFuncProtocolModelApi {

    public DefaultMqttPublishProtocolModelApi(String code, String name) {
        super(code, name, FuncType.W);
    }

    @Override
    protected Protocol buildProtocol(ProtocolModelApiInvokeParam arg) {
        IotClient client = FrameworkManager.getClient(arg.getParentDeviceSn(), DefaultMqttMessage.class);
        if(client == null) {
            throw new ProtocolInvokeException("客户端不存在["+arg.getParentDeviceSn()+"]");
        }

        String topic = arg.getParam().getString("topic");
        topic = StrFormatter.format(topic, arg.getConfig(), true);
        byte[] msg = arg.getParam().getString("payload").getBytes(StandardCharsets.UTF_8);
        return new DefaultMqttPublishProtocol(msg, topic).setClientKey(client.getConfig());
    }

    @Override
    protected void doInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) {
        DefaultMqttPublishProtocol publishProtocol = (DefaultMqttPublishProtocol) protocol;
        publishProtocol.request(protocol1 -> {
            if(protocol1.getExecStatus() == ExecStatus.success) {
                result.accept(InvokeResult.success(protocol1));
            } else {
                result.accept(InvokeResult.status(protocol1.getExecStatus(), protocol1));
            }
        });
    }
}
