package com.iteaj.framework.spi.event;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.function.Consumer;

/**
 * create time: 2021/3/31
 * @see ApplicationListener#forPayload(Consumer)
 * @author iteaj
 * @since 1.0
 */
public final class EventUtils implements ApplicationContextAware {

    /**
     * 默认使用spring事件机制
     */
    private static ApplicationContext applicationContext;

    public static void publish(PayloadEvent event) {
        applicationContext.publishEvent(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
