package me.x1ao.jetlinks.event;

import me.x1ao.jetlinks.msg.DeviceInvokeFunctionMessage;
import org.springframework.context.ApplicationEvent;

/**
 * 功能调用事件
 */
public class DeviceFunctionInvokeEvent extends ApplicationEvent {

    private final DeviceInvokeFunctionMessage message;

    /**
     * 构造函数
     *
     * @param source 事件来源对象
     * @param message 消息内容
     */
    public DeviceFunctionInvokeEvent(Object source, DeviceInvokeFunctionMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * 获取功能调用的消息体
     *
     * @return 设备功能调用消息
     */
    public DeviceInvokeFunctionMessage getMessage() {
        return message;
    }
}
