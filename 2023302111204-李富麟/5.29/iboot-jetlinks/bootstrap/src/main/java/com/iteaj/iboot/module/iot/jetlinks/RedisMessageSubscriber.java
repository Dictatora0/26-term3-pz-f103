package com.iteaj.iboot.module.iot.jetlinks;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.iboot.module.iot.controller.DeviceCtrlController;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.x1ao.jetlinks.msg.DeviceFunctionInput;
import me.x1ao.jetlinks.msg.DeviceInvokeFunctionMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class RedisMessageSubscriber {

    public static final String REDIS_CHANNEL = "websocket_channel";
    private final DeviceCtrlController deviceCtrlController;
    private final IProductService productService;
    private final IDeviceService deviceService;

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener((message, pattern) -> {
            String receivedMessage = new String(message.getBody());
            log.info("从 Redis 收到消息: {}", receivedMessage);

            // 处理消息
            DeviceInvokeFunctionMessage deviceInvokeFunctionMessage = JSONObject.parseObject(receivedMessage, DeviceInvokeFunctionMessage.class);

            // 设备标识
            String deviceId = deviceInvokeFunctionMessage.getFunctionId();
            List<DeviceFunctionInput> inputs = deviceInvokeFunctionMessage.getInputs();

            inputs.stream().filter(input -> input.getName().equals("code")).findFirst().ifPresent(input -> {
                String value = input.getValue();

                ProtocolModelApiInvokeParam param = new ProtocolModelApiInvokeParam(deviceId);
                DetailResult<Device> detailResult = deviceService.getByUid(deviceId);
                DetailResult<ProductDto> productDtoDetailResult = productService.debugById(detailResult.of().get().getProductId());

                JSONObject jsonObject = new JSONObject();
                productDtoDetailResult.of().ifPresent(productDto -> {
                    for (ModelApi funcApi : productDto.getFuncApis()) {
                        funcApi.getDownConfig().forEach(config -> {
                            jsonObject.put(config.getProtocolAttrField(), config.getValue());
                        });
                    }
                });
                param.setParam(jsonObject);

                deviceCtrlController.run(value, param);

            });

        }, new ChannelTopic(REDIS_CHANNEL));

        return container;
    }
}
