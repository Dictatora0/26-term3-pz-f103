package com.iteaj.iboot.plugin.quartz.jobs;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzTask {

    /**
     * bean名称
     * @see Component#value()
     * @return
     */
    String value() default "";

    /**
     * 作业说明
     * @return
     */
    String desc() default "";
}
