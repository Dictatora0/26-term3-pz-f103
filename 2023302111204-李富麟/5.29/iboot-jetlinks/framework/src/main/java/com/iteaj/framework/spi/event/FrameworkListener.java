package com.iteaj.framework.spi.event;

import org.springframework.context.ApplicationListener;

/**
 * create time: 2020/4/21
 *  事件监听器
 * @author iteaj
 * @since 1.0
 */
public interface FrameworkListener<E extends PayloadEvent> extends ApplicationListener<E> {

}
