package me.x1ao.jetlinks.event;

import me.x1ao.jetlinks.msg.DeviceWritePropertyMessage;
import org.springframework.context.ApplicationEvent;

/**
 * 属性写入事件
 */
public class DevicePropertyWriteEvent extends ApplicationEvent {

    private final DeviceWritePropertyMessage message;

    /**
     * 构造函数
     *
     * @param source 事件来源对象
     * @param message 消息内容
     */
    public DevicePropertyWriteEvent(Object source, DeviceWritePropertyMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * 获取属性写入的消息体
     *
     * @return 设备属性写入消息
     */
    public DeviceWritePropertyMessage getMessage() {
        return message;
    }
}
