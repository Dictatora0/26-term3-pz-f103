package com.iteaj.framework.spring.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * create time: 2021/3/21
 *
 * @author iteaj
 * @since 1.0
 */
public class RedisConditionalImpl implements Condition {

    private static Boolean hasRedis = true;

    static {
        try {
            Class.forName("org.springframework.data.redis.connection.RedisConnectionFactory");
        } catch (ClassNotFoundException e) {
            hasRedis = false;
        }
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if(hasRedis) {
            return context.getRegistry().containsBeanDefinition("redisTemplate");
        }

        return false;
    }
}
