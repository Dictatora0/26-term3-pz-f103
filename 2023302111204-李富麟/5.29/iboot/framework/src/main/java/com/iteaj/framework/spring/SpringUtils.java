package com.iteaj.framework.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringUtils implements ApplicationContextAware {

    private static SpringUtils instance = new SpringUtils();
    private static ConfigurableApplicationContext applicationContext;

    protected SpringUtils() { }

    public static SpringUtils getInstance() {
        return instance;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    public static  <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }
}
