package com.iteaj.iboot.plugin.quartz.jobs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzJob {

    /**
     * 作业说明
     * @return
     */
    String value() default "";

}
