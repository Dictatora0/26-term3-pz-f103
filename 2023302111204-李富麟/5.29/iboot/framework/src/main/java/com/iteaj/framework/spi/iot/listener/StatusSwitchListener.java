package com.iteaj.framework.spi.iot.listener;

/**
 * 状态切换监听
 */
public interface StatusSwitchListener extends IotListener<EntityPayload>{

    /**
     *  状态切换事件
     * @param eventType
     * @param entity
     */
    @Override
    void onIotEvent(IotEventType eventType, EntityPayload entity);
}
