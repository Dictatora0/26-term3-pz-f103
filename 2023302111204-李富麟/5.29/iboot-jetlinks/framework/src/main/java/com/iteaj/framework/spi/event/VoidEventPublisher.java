package com.iteaj.framework.spi.event;

import java.util.Collections;
import java.util.List;

/**
 * create time: 2020/4/24
 *  空的事件发布
 * @author iteaj
 * @since 1.0
 */
public class VoidEventPublisher implements EventPublisher {

    public VoidEventPublisher() {
        LOGGER.warn("正在使用VoidEventPublisher, 所有的事件({})将不会被发布", FrameworkListener.class.getSimpleName());
    }

    @Override
    public void publish(FrameworkEvent event) {
        LOGGER.warn("正在使用{}事件发布器, 将导致无任何事件被发布, 监听到事件: {}", VoidEventPublisher.class, event);
    }

    @Override
    public List<FrameworkListener> listeners() {
        return Collections.EMPTY_LIST;
    }
}
