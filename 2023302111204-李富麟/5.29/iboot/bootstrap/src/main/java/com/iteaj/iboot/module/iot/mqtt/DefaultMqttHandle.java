package com.iteaj.iboot.module.iot.mqtt;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.iteaj.framework.spi.iot.*;
import com.iteaj.framework.spi.iot.consts.DataType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.framework.spi.iot.listener.EntityPayload;
import com.iteaj.framework.spi.iot.listener.IotEvenPublisher;
import com.iteaj.framework.spi.iot.listener.IotEventType;
import com.iteaj.framework.spi.iot.listener.StatusSwitchListener;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectListenerManager;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelApiConfig;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iot.client.ClientProtocolHandle;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttConnectProperties;
import com.iteaj.iot.client.mqtt.impl.DefaultMqttSubscribeProtocol;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DefaultMqttHandle implements ClientProtocolHandle<DefaultMqttSubscribeProtocol>, StatusSwitchListener {

    private final static String clientId = "clientId";
    private final static String deviceSn = "deviceSn";
    private final static String gatewaySn = "gatewaySn";
    private final static String DEFAULT_MQTT_IMPL = "MQTT_DEFAULT_IMPL";
    private final IotCacheManager cacheManager;
    private final IProductService productService;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Map<String, SubscribeConfig>> productSubscribeApis = new ConcurrentHashMap<>();

    public DefaultMqttHandle(IotCacheManager cacheManager, IProductService productService) {
        this.cacheManager = cacheManager;
        this.productService = productService;
    }

    @Override
    public Object handle(DefaultMqttSubscribeProtocol protocol) {

        String protocolTopic = protocol.getTopic(); // 设备发布的topic
        String[] subscribeTopic = protocolTopic.split("/");
        Map<String, SubscribeConfig> subscribeApis = productSubscribeApis.get(subscribeTopic[0]);
        if(CollectionUtil.isNotEmpty(subscribeApis)) {
            subscribeApis.forEach((topic, config) -> {
                String[] modelTopic = topic.split("/");
                if(modelTopic.length != subscribeTopic.length) { // 不匹配
                    if(logger.isDebugEnabled()) {
                        logger.debug("mqtt订阅不匹配 topic长度不一致 平台订阅: {} - 设备发布: {}", topic, protocolTopic);
                    }

                    return;
                }

                DefaultMqttConnectProperties properties = protocol.requestMessage().getProperties();
                String clientIdValue, deviceSnValue = null, gatewaySnValue = properties.connectKey();
                for (int i = 0; i < modelTopic.length; i++) {
                    String item = modelTopic[i];
                    String subscribe = subscribeTopic[i];
                    if(item.startsWith("{") && item.endsWith("}")) { // 如果是占位符, 解析占位符的值
                        if(item.contains(deviceSn)) {
                            deviceSnValue = subscribe;
                        } else if(item.contains(clientId)) {
                            clientIdValue = subscribe;
                            // 只匹配同一个clientId下的
                            if(!properties.getClientId().equals(clientIdValue)) {
                                if(logger.isDebugEnabled()) {
                                    logger.debug("mqtt订阅不匹配 不属于平台指定客户端 平台客户端: {} - 设备发布: {}"
                                            , properties.getClientId(), clientIdValue);
                                }

                                return;
                            }
                        } else if(item.contains(gatewaySn)) {
                            gatewaySnValue = subscribe;
                            // 只匹配同一网关下的
                            if(!gatewaySnValue.equals(properties.connectKey())) {
                                if(logger.isDebugEnabled()) {
                                    logger.debug("mqtt订阅不匹配 不属于统一网关 平台配置: {} - 设备发布: {}"
                                            , properties.connectKey(), gatewaySnValue);
                                }

                                return;
                            }
                        }
                    } else if(item.equals(subscribe)) {
                        continue; // 如果相等则继续判断
                    } else {
                        return; // 只要有一个项不相等则不匹配
                    }
                }

                if(deviceSnValue == null) {
                    if(logger.isWarnEnabled()) {
                        logger.warn("mqtt订阅不匹配(平台topic未指定占位符{deviceSn}) 未指定设备编号 平台订阅: {}", topic);
                    }

                    return;
                }

                DeviceKey deviceKey = DeviceKey.build(deviceSnValue, gatewaySnValue);
                RealtimeStatus device = cacheManager.get(DEFAULT_MQTT_IMPL, deviceKey);

                try {
                    if(device != null) {
                        if(device.getStatus() == DeviceStatus.offline && !config.isWill()) {
                            device.setStatus(DeviceStatus.online); // 设备上线
                            IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
                        } else if(config.isWill()) {
                            device.setStatus(DeviceStatus.offline); // 设备离线
                            IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
                        }

                        if(CollectionUtil.isNotEmpty(config.getAttrs())) {
                            resolvePayloadValue(config.getAttrs(), protocol, gatewaySnValue, deviceSnValue, device);
                        }
                    } else {
                        logger.error("mqtt解析topic失败 设备不存在 - 设备: {} - 平台订阅: {} - 设备发布: {}", deviceKey, topic, protocolTopic);
                    }
                } catch (Exception e) {
                    logger.error("处理mqtt报文负载到物模型属性失败 未知错误 - 平台订阅: {} - 设备发布: {}", topic, protocolTopic, e);
                }
            });
        } else {
            if(logger.isWarnEnabled()) {
                logger.warn("mqtt处理失败(没有找到Mqtt产品) 没有配置产品或产品未启用 - 设备发布: {}", protocolTopic);
            }
        }

        return null;
    }

    private void resolvePayloadValue(List<UpModelAttr> dicts, DefaultMqttSubscribeProtocol protocol
            , String gatewaySnValue, String deviceSnValue, RealtimeStatus device) {
        Date date = new Date();
        byte[] message = protocol.requestMessage().getMessage();
        JSONObject jsonObject = JSONObject.parseObject(new String(message, StandardCharsets.UTF_8));
        List<SignalOrFieldValue> fieldValues = dicts.stream().map(item -> {
            Object fieldValue;
            if(StrUtil.isBlank(item.getPath())) {
                fieldValue = jsonObject.get(item.getField());
            } else {
                fieldValue = JSONPath.eval(jsonObject, item.getPath());
            }

            Object value = DataValueResolverFactory.resolver(item, fieldValue);
            return new SignalOrFieldValue(item.getId(), item.getField(), date, value);
        }).collect(Collectors.toList());

        // 发布模型属性的事件
        DataSupplier dataSupplier = new DataSupplier(DEFAULT_MQTT_IMPL, DeviceKey.build(deviceSnValue, gatewaySnValue));
        dataSupplier.setValues(fieldValues);
        CollectListenerManager.getInstance().modelAttrPublish(modelAttrListener -> {
            modelAttrListener.supplier(dataSupplier, device);
        });
    }

    private void syncSubscribeApi(String productCode) {
        productService.listByProtocolCode(DEFAULT_MQTT_IMPL, productCode).forEach(productDto -> {
            if(CollectionUtil.isEmpty(productDto.getEventApis())) {
                return; // 没有订阅事件直接返回
            }

            if(CollectionUtil.isEmpty(productDto.getAttrs())) {
                logger.error("解析mqtt物模型失败 产品没有配置模型属性 - 产品: {}", productDto.getName());
                return;
            }

            // 产品没有启用则不进行匹配
            if(productDto.getStatus() == FuncStatus.disabled) {
                logger.warn("解析mqtt物模型失败 产品未启用 - 产品: {}", productDto.getName());
                return;
            }

            List<ModelApi> eventApis = productDto.getEventApis();
            if(CollectionUtil.isNotEmpty(eventApis)) {
                Map<String, SubscribeConfig> subscribeApis = new HashMap<>();
                eventApis.forEach(api -> {
                    if(CollectionUtil.isNotEmpty(api.getUpConfig())) {
                        String topic = null; List<UpModelAttr> attrs = new ArrayList<>(); boolean isWill = false;
                        for (int i = 0; i < api.getUpConfig().size(); i++) {
                            ModelApiConfig config = api.getUpConfig().get(i);
                            if("topic".equals(config.getProtocolAttrField())) {
                                if(config.getModelAttrId() != null) {
                                    logger.error("解析mqtt物模型协议失败 topic不支持属性提及 - 产品: {}", productDto.getName(), new IllegalArgumentException("topic不支持属性提及"));
                                    return;
                                } else {
                                    topic = config.getValue();
                                }
                            } else if("payload".equals(config.getProtocolAttrField())) {
                                if(config.getModelAttrId() == null) {
                                    if(logger.isDebugEnabled()) {
                                        logger.debug("解析mqtt物模型 payload没有配置属性 - 产品: {} - 事件模型: {}/{}"
                                                , productDto.getName(), api.getName(), api.getCode());
                                    }
                                } else if(CollectionUtil.isEmpty(config.getDicts())) {
                                    if(logger.isDebugEnabled()) {
                                        logger.debug("解析mqtt物模型 payload属性没有配置项 - 产品: {} - 事件模型: {}/{}"
                                                , productDto.getName(), api.getName(), api.getCode());
                                    }
                                } else {
                                    attrs = config.getDicts().stream().map(item -> new UpModelAttr(Long.valueOf(item.getDictValue())
                                            , item.getAttrField(), item.getDictName(), DataType.build(item.getDataType())
                                            , item.getAccuracy(), item.getGain(), item.getResolver(), item.getScript()
                                            , item.getPath())).collect(Collectors.toList());
                                }

                            } else {
                                isWill = Boolean.parseBoolean(config.getValue());
                            }
                        }

                        subscribeApis.put(topic, new SubscribeConfig(isWill, attrs));
                    }
                });

                productSubscribeApis.put(productCode, subscribeApis);
            }
        });
    }

    @Override
    public boolean isEventMatcher(IotEventType eventType) {
        return eventType == IotEventType.ProductSwitch;
    }

    @Override
    public void onIotEvent(IotEventType eventType, EntityPayload payload) {
        ProductDto productDto = (ProductDto) payload.getPayload();

        // Mqtt网关子设备
        if(TransportProtocol.MQTT == productDto.getTransportProtocol()
                && DeviceType.Child == productDto.getDeviceType()) {
            if(productDto.getStatus() == FuncStatus.enabled) {
                syncSubscribeApi(productDto.getCode());
            } else {
                productSubscribeApis.remove(productDto.getCode());
            }
        }
    }

    @Getter
    public static class SubscribeConfig {
        /**
         * 是否遗嘱
         */
        private final boolean isWill;

        /**
         * 属性配置
         */
        private final List<UpModelAttr> attrs;

        public SubscribeConfig(boolean isWill, List<UpModelAttr> attrs) {
            this.isWill = isWill;
            this.attrs = attrs;
        }
    }
}
