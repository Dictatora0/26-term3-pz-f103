package com.iteaj.framework.spring.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create time: 2021/3/21
 *  是否引入redis
 * @author iteaj
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(RedisConditionalImpl.class)
public @interface ConditionalOnRedis {

}
