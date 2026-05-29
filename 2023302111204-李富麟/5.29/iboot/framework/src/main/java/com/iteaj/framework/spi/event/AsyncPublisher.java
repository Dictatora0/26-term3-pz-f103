package com.iteaj.framework.spi.event;

/**
 * create time: 2020/4/21
 *  异步发布者
 * @author iteaj
 * @since 1.0
 */
public interface AsyncPublisher extends EventPublisher {

    /**
     * 读取发布的事件
     */
    void readEvent();
}
