package com.iteaj.plugin.protocol.mqtt.impl;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Map;
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
        byte[] msg = buildPayload(arg).getBytes(StandardCharsets.UTF_8);
        return new DefaultMqttPublishProtocol(msg, topic).setClientKey(client.getConfig());
    }

    private String buildPayload(ProtocolModelApiInvokeParam arg) {
        String payload = arg.getParam().getString("payload");
        if(StrUtil.isBlank(payload)) {
            JSONObject json = new JSONObject();
            for(Map.Entry<String, Object> entry : arg.getParam().entrySet()) {
                String key = entry.getKey();
                if("topic".equals(key) || "payload".equals(key)) {
                    continue;
                }

                json.put(key, entry.getValue());
            }

            if(json.isEmpty()) {
                throw new ProtocolInvokeException("鍙戝竷payload涓嶈兘涓虹┖");
            }

            return JSON.toJSONString(json);
        }

        return normalizePayload(payload);
    }

    private String normalizePayload(String payload) {
        String trim = payload.trim();
        if(trim.startsWith("{") && trim.endsWith("}")) {
            String normalized = trim.replaceAll("([\\\\{,]\\\\s*)([A-Za-z_][A-Za-z0-9_]*)\\\\s*:", "$1\\\"$2\\\":");
            try {
                return JSON.toJSONString(JSON.parse(normalized));
            } catch (Exception e) {
                return normalized;
            }
        }

        return payload;
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
