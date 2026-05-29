package com.iteaj.iboot.module.iot.collect.store;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.iboot.module.iot.collect.CollectException;
import com.iteaj.iboot.module.iot.consts.IotConsts;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iot.client.mqtt.gateway.*;
import com.iteaj.iot.message.DefaultMessageBody;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class MqttStoreAction extends StoreAction {

    private static ExpressionParser parser = new SpelExpressionParser();

    @Override
    public String getName() {
        return IotConsts.STORE_ACTION_MQTT;
    }

    @Override
    public String getDesc() {
        return "MQTT服务";
    }

    @Override
    public void configValidate(Map<String, Object> jsonConfig) {
        if(!(jsonConfig.get("topic") instanceof String)) {
            throw new CollectException("未配置参数[topic]");
        }
        if(!(jsonConfig.get("clientId") instanceof String)) {
            throw new CollectException("未配置参数[clientId]");
        }
        if(!(jsonConfig.get("host") instanceof String)) {
            throw new CollectException("未配置参数[host]");
        }
        if(!(jsonConfig.get("port") instanceof Integer)) {
            throw new CollectException("未配置参数[port]");
        }
    }

    @Override
    public void store(CollectDetail detail, List<CollectData> data) {
        data.forEach(item -> {
            Map<String, Object> config = detail.resolveConfig();
            // 解析数据表表名
            String topic = parser.parseExpression((String) config.get("topic")).getValue(item, String.class);

            // 创建连接配置信息
            MqttGatewayConnectProperties connectProperties = new MqttGatewayConnectProperties((String) config.get("host")
                    , (Integer) config.get("port"), (String) config.get("clientId"), (String) config.get("topic"));
            if(StringUtils.hasText((String) config.get("username")) &&
                    StringUtils.hasText((String) config.get("password"))) {
                connectProperties.setPassword((String) config.get("password"));
                connectProperties.setUsername((String) config.get("username"));
            }

            // 创建请求报文
            MqttGatewayHead head = new MqttGatewayHead((String) config.get("clientId")
                    , null, MqttGatewayProtocolType.Mqtt_Gateway);
            DefaultMessageBody body = new DefaultMessageBody(JSONObject.toJSONBytes(item));
            MqttGatewayMessage gatewayMessage = new MqttGatewayMessage(head, body, MqttQoS.AT_LEAST_ONCE, topic);

            // 发送请求给mqtt服务器
            new MqttGatewayProtocol(gatewayMessage).request(connectProperties);
        });
    }
}
