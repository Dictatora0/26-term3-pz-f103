package com.iteaj.iboot.module.iot.mqtt;

import com.iteaj.iot.client.mqtt.impl.DefaultMqttConnectProperties;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttSubscribeProtocol;
import com.iteaj.iot.client.mqtt.impl.MqttSubscribeListener;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.springframework.stereotype.Component;

//@Component
public class MqttDebugSubscribeListener implements MqttSubscribeListener {

    private MqttTopicSubscription subscription = new MqttTopicSubscription("iot/debug/#", MqttQoS.AT_MOST_ONCE);

    @Override
    public void onSubscribe(DefaultMqttSubscribeProtocol protocol) {

    }

    @Override
    public MqttTopicSubscription topic(DefaultMqttConnectProperties client) {
        return subscription;
    }
}
