package com.iteaj.framework.autoconfigure;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.iteaj.framework.json.JacksonUtils;
import com.iteaj.framework.utils.RedisUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * create time: 2021/7/3
 *  redis缓存基于jackson序列化实现
 * @author iteaj
 * @since 1.0
 */
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisJsonAutoConfiguration {

    @Primary
    @Bean({"redisTemplate", "jsonRedisTemplate"})
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public RedisTemplate jacksonRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key使用String序列化
        redisTemplate.setKeySerializer(redisTemplate.getStringSerializer());
        redisTemplate.setHashKeySerializer(redisTemplate.getStringSerializer());

        // value 使用Jackson序列化
        Jackson2JsonRedisSerializer<Object> valueSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        valueSerializer.setObjectMapper(JacksonUtils.getGenericMapper());

        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        return redisTemplate;
    }

    @Bean({"redisTemplate", "jsonRedisTemplate"})
    @ConditionalOnClass(name = "com.alibaba.fastjson.support.spring.FastJsonRedisSerializer")
    public RedisTemplate fastjsonRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key使用String序列化
        redisTemplate.setKeySerializer(redisTemplate.getStringSerializer());
        redisTemplate.setHashKeySerializer(redisTemplate.getStringSerializer());

        // value 使用Fastjson序列化
        GenericFastJsonRedisSerializer valueSerializer =
                new GenericFastJsonRedisSerializer();

        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        return redisTemplate;
    }

    @Bean
    public RedisUtils redisUtils(RedisTemplate template) {
        return new RedisUtils(template);
    }
}
