package com.iteaj.framework.utils;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

public class RedisUtils {

    private static RedisTemplate template;
    private static SetOperations setOperations;
    private static HashOperations hashOperations;
    private static ValueOperations valueOperations;

    public RedisUtils(RedisTemplate template) {
        RedisUtils.template = template;
        hashOperations = template.opsForHash();
        valueOperations = template.opsForValue();
        setOperations = template.opsForSet();
    }

    public static RedisTemplate getTemplate() {
        return template;
    }

    public static HashOperations getOpsForHash() {
        return hashOperations;
    }

    public static ValueOperations getOpsForValue() {
        return valueOperations;
    }

    public static SetOperations getOpsForSet() {
        return setOperations;
    }
}
