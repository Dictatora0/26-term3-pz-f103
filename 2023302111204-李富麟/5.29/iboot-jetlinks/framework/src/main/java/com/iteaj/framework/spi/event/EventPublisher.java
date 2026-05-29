package com.iteaj.framework.spi.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * create time: 2020/4/21
 *  事件发布者
 * @author iteaj
 * @since 1.0
 */
public interface EventPublisher {

    Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

    void publish(FrameworkEvent event);

    /**
     * 监听列表
     * @return
     */
    List<FrameworkListener> listeners();
}
