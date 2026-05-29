package com.iteaj.framework.spi.iot.listener;

/**
 * @see RealtimePayload
 * @see IotEventType#Realtime
 */
public interface RealtimeInfoListener extends IotListener<RealtimePayload>{

    @Override
    default boolean isEventMatcher(IotEventType eventType) {
        return eventType == IotEventType.Realtime;
    }
}
