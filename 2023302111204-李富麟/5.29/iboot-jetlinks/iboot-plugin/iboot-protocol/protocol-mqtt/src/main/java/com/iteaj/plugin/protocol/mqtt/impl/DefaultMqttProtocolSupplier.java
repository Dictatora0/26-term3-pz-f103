package com.iteaj.plugin.protocol.mqtt.impl;

import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.NetworkConfigImpl;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.StaticClientProtocolSupplier;
import com.iteaj.framework.spi.iot.consts.*;
import com.iteaj.framework.spi.iot.protocol.*;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.client.mqtt.MqttClient;
import com.iteaj.iot.client.mqtt.MqttClientComponent;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttComponent;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttConnectProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultMqttProtocolSupplier extends StaticClientProtocolSupplier<NetworkConfigImpl.MqttNetworkConfig, MqttClientComponent, MqttClient> {

    private Map<String, ProtocolModelAttr> modelAttrs = new LinkedHashMap<>();
    private Map<String, AbstractProtocolModelApi> modelApis = new LinkedHashMap<>();

    public DefaultMqttProtocolSupplier() {
        modelAttrs.put("will", new ProtocolModelAttr("will", "遗嘱", DataType.t_boolean, AttrType.IGNORE, "是否为遗嘱消息"));
        modelAttrs.put("topic", new ProtocolModelAttr("topic", "主题", DataType.t_any, AttrType.IGNORE, "支持使用占位符{deviceSn}{gatewaySn}"));
        modelAttrs.put("payload", new ProtocolModelAttr("payload", "负载", DataType.t_json, AttrType.IGNORE));
        modelApis.put("publish", new DefaultMqttPublishProtocolModelApi("publish", "发布")
                .addDownConfig(ProtocolModelApiConfig.downBuild("topic", "发布的主题, 支持占位符{clientId}{deviceSn}{gatewaySn}", 1))
                .addDownConfig(ProtocolModelApiConfig.downBuild("payload", "发布的数据", 2)));
        modelApis.put("subscribe", new DefaultMqttSubscribeProtocolModelApi("subscribe", "订阅")
                .addUpConfig(ProtocolModelApiConfig.upBuild("topic", "以网关子设备的产品编码开头且需包含设备编号占位符, 支持占位符{clientId}{deviceSn}{gatewaySn}", 1))
                .addUpConfig(ProtocolModelApiConfig.upBuild("will", "是否为遗嘱消息").addOption("是", "true").addOption("否", "false"))
                .addUpConfig(ProtocolModelApiConfig.upBuild("payload", "订阅的数据", 2)));
    }

    @Override
    protected MqttClient doCreateClient(NetworkConfigImpl.MqttNetworkConfig config, MqttClientComponent component) {
        Integer qos = config.getQos();
        String[] split = config.getTopics().split(",");
        List<MqttTopicSubscription> subscriptions = new ArrayList<>();
        if(split != null) {
            MqttQoS mqttQoS = MqttQoS.valueOf(qos);
            for (int i = 0; i < split.length; i++) {
                subscriptions.add(new MqttTopicSubscription(split[i], mqttQoS));
            }
        }

        DefaultMqttConnectProperties connectProperties = new DefaultMqttConnectProperties(config.getHost(), config.getPort(), config.getClientId(), subscriptions, null);
        connectProperties.setUsername(config.getUsername());
        connectProperties.setPassword(config.getPassword());
        connectProperties.setConnectKey(config.getDeviceSn()); // 使用设备编号作为唯一标识
        if(config.getKeepalive() > 0) {
            connectProperties.setKeepAlive(config.getKeepalive());
        }
        return new MqttClient(component, connectProperties);
    }

    @Override
    public String getDesc() {
        return "默认Mqtt协议";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ProtocolModel getProtocol() {
        return new DefaultProtocolModel(ProtocolCodes.MQTT_DEFAULT_IMPL.getValue(), CtrlMode.COMMON, TransportProtocol.MQTT, modelApis, modelAttrs);
    }

    @Override
    public ParamMeta[] getGatewayConfig() {
        return NetworkConfigImpl.MqttNetworkConfig.mqttMetas;
    }

    @Override
    public GatewayType getGatewayType() {
        return GatewayType.Gateway;
    }

    @Override
    public ProtocolImplMode getImplMode() {
        return ProtocolImplMode.Internal;
    }

    @Override
    public DataSupplier getDataSupplier(Protocol protocol, ProtocolModelApiInvokeParam param, Object value) {
        return null;
    }

    @Override
    protected MqttClientComponent doCreateComponent(NetworkConfigImpl.MqttNetworkConfig config) {
        return new DefaultMqttComponent();
    }
}
