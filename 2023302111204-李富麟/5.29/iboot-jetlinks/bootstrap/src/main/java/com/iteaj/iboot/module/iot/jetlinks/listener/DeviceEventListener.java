package com.iteaj.iboot.module.iot.jetlinks.listener;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.iboot.module.iot.jetlinks.RedisMessageSubscriber;
import lombok.AllArgsConstructor;
import me.x1ao.jetlinks.event.DeviceFunctionInvokeEvent;
import me.x1ao.jetlinks.event.DevicePropertyWriteEvent;
import me.x1ao.jetlinks.msg.DeviceInvokeFunctionMessage;
import me.x1ao.jetlinks.msg.DeviceWritePropertyMessage;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 设备消息事件监听器
 */
@Component
@AllArgsConstructor
public class DeviceEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 监听功能调用事件
     */
    @EventListener
    public void handleFunctionInvoke(DeviceFunctionInvokeEvent event) {
        DeviceInvokeFunctionMessage message = event.getMessage();
        // 发布到 Redis 频道
        redisTemplate.convertAndSend(RedisMessageSubscriber.REDIS_CHANNEL, JSONObject.toJSONString(message));

    }

    /**
     * 监听属性写入事件
     */
    @EventListener
    public void handlePropertyWrite(DevicePropertyWriteEvent event) {
        DeviceWritePropertyMessage message = event.getMessage();
        System.out.println("【属性写入】收到请求");

        message.getProperties().forEach((key, value) -> {
            System.out.println("属性名: " + key + ", 值: " + value);
        });
    }
}
