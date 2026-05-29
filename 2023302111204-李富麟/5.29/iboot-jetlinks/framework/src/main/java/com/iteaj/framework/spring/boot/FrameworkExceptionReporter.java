package com.iteaj.framework.spring.boot;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

public class FrameworkExceptionReporter implements SpringBootExceptionReporter {

    private final ConfigurableApplicationContext applicationContext;

    public FrameworkExceptionReporter(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean reportException(Throwable failure) {
        return false;
    }
}
