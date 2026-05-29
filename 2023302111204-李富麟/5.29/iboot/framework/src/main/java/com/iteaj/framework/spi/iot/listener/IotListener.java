package com.iteaj.framework.spi.iot.listener;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * 物联网模块事件监听器
 * @param <V> 事件值
 */
public interface IotListener<V> extends EventListener, Ordered, Comparable<IotListener> {

    /**
     * 是否匹配事件, 匹配则执行 {@link #onIotEvent(IotEventType, Object)}
     * @param eventType
     * @return 匹配状态
     */
    boolean isEventMatcher(IotEventType eventType);

    /**
     * 监听事件
     * @param eventType
     * @param value
     */
    void onIotEvent(IotEventType eventType, V value);

    /**
     * 监听事件
     * @param eventType
     * @param value
     */
    default void onIotEvent(IotEventType eventType, V value, Consumer callback) {

    }

    @Override
    default int getOrder() {
        return 168000;
    }

    @Override
    default int compareTo(@NotNull IotListener o) {
        int compare = this.getOrder() - o.getOrder();
        return compare > 0 ? 1 : compare == 0 ? 0 : -1;
    }
}
