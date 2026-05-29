package com.iteaj.framework.logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create time: 2021/7/4
 *  web访问日志
 * @author iteaj
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

    /**
     * 日志说明
     * @return
     */
    String value();

    /**
     * 日志类型
     * @return
     */
    LoggerType type() default LoggerType.Func;
}
