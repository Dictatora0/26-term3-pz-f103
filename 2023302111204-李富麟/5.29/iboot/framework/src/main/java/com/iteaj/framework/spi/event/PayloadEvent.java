package com.iteaj.framework.spi.event;

import org.springframework.context.PayloadApplicationEvent;

/**
 * create time: 2020/4/21
 *  事件
 * @author iteaj
 * @since 1.0
 */
public class PayloadEvent<T> extends PayloadApplicationEvent<T> {

    public PayloadEvent(Object source, T payload) {
        super(source, payload);
    }
}
