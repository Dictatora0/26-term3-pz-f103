package com.iteaj.framework.spi.iot.listener;

import lombok.Getter;
import lombok.Setter;

/**
 * 实体负载
 */
@Getter
@Setter
public class EntityPayload<T> implements IotEventPayload{

    private T payload;

    public EntityPayload(T payload) {
        this.payload = payload;
    }
}
