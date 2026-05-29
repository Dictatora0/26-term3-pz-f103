package com.iteaj.iboot.module.iot.debug.mqtt;

import com.iteaj.framework.result.HttpResult;
import com.iteaj.iboot.module.iot.debug.DebugHandle;
import com.iteaj.iboot.module.iot.debug.DebugResult;
import com.iteaj.iboot.module.iot.debug.DebugWebsocketWrite;
import com.iteaj.iot.client.mqtt.MqttClient;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttComponent;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttConnectProperties;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttPublishProtocol;
import com.iteaj.iot.consts.ExecStatus;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class MqttDebugHandle implements DebugHandle<MqttDebugModel> {

    @Autowired(required = false)
    private DefaultMqttComponent defaultMqttComponent;

    @Override
    public String type() {
        return "mqtt";
    }

    @Override
    public void handle(MqttDebugModel model, DebugWebsocketWrite write) {
        if(!StringUtils.hasText(model.getIp()) || model.getPort() == null) {
            write.write(HttpResult.Fail("未指定mqtt设备的ip地址和端口")); return;
        }

        DebugResult result = new DebugResult(model);
        try{
            DefaultMqttConnectProperties properties = new DefaultMqttConnectProperties(model.getIp()
                    , model.getPort(), model.getDeviceSn(), null, null);
            MqttClient client = defaultMqttComponent.getClient(properties);
            if(client == null) {
                write.write(HttpResult.Fail("客户端未连接")); return;
            }

            // 订阅
            if(model.getCmd().equals("subscription")) {
                defaultMqttComponent.subscribe(model.getDeviceSn(), model.getTopic(), MqttQoS.AT_LEAST_ONCE);
            } else if(model.getCmd().equals("publish")) { // 发布
                DefaultMqttPublishProtocol protocol = new DefaultMqttPublishProtocol(model
                        .getValue().getBytes(StandardCharsets.UTF_8), "iot/test/debug");
                protocol.request(properties);
                if(protocol.getExecStatus() == ExecStatus.success) {
                    write.write(HttpResult.Success(result));
                } else {
                    write.write(HttpResult.StatusCode(result, protocol.getExecStatus().desc, 208));
                }
            }
//            result.setValue(readValue).setRespTime(System.currentTimeMillis())
//                .setReqMsg(ByteUtil.bytesToHexByFormat(plcClientProtocol.requestMessage().getMessage()))
//                .setRespMsg(ByteUtil.bytesToHexByFormat(plcClientProtocol.responseMessage().getMessage()));
//            ExecStatus execStatus = plcClientProtocol.getExecStatus();
//            if(execStatus != ExecStatus.success) {
//                write.write(HttpResult.StatusCode(result, execStatus.desc, 208));
//            } else {
//                write.write(HttpResult.Success(result));
//            }

        } catch (Exception e) {
            write.write(HttpResult.Fail(e.getMessage()));
        }
    }
}
