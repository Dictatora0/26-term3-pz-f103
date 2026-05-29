package com.iteaj.framework.autoconfigure;

import com.iteaj.framework.cache.CacheEntry;
import com.iteaj.framework.cache.SerializerType;
import com.iteaj.framework.spring.condition.ConditionalOnCluster;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * create time: 2021/4/3
 *
 * @author iteaj
 * @since 1.0
 */
@EnableCaching
@ImportAutoConfiguration(CacheManagerAutoConfiguration.RedisConfiguration.class)
public class CacheManagerAutoConfiguration {

    /**
     * 默认的缓存
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({CacheManager.class})
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    /**
     * Redis缓存配置
     */
    @ConditionalOnCluster
    @ConditionalOnClass(name = {"org.springframework.data.redis.connection.RedisConnectionFactory"})
    public static class RedisConfiguration {

        @Primary
        @Bean({"cacheManager", "redisCacheManager"})
        public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, ObjectProvider<CacheEntry> caches) {
            RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory);

            final ClassLoader classLoader = getClass().getClassLoader();

            // 创建默认的redis缓存配置
            RedisCacheConfiguration defaultCacheConfiguration = this.defaultCacheConfiguration(classLoader);

            Map<String, RedisCacheConfiguration> initCaches = new HashMap<>();

            // 创建session缓存配置
            caches.forEach(item -> {
                initCaches.put(item.getName(), doCreateCache(item, classLoader));
            });

            return new RedisCacheManager(redisCacheWriter, defaultCacheConfiguration, initCaches);
        }

        private RedisCacheConfiguration doCreateCache(CacheEntry item, ClassLoader classLoader) {
            RedisSerializationContext.SerializationPair serializer;
            if(item.getType() == SerializerType.Jdk) {
                // 使用jdk序列化
                serializer = RedisSerializationContext.SerializationPair
                        .fromSerializer(new JdkSerializationRedisSerializer(classLoader));
            } else {
                // 使用jackson序列化
                serializer = RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(item.getClazz()));
            }

            RedisCacheConfiguration sessionCache = RedisCacheConfiguration
                    .defaultCacheConfig().serializeValuesWith(serializer);

            // 设置session缓存时间
            Duration timeToLiveSeconds = Duration.ofSeconds(item.getExpire());
            sessionCache.entryTtl(timeToLiveSeconds);

            return sessionCache;
        }

        public RedisCacheConfiguration defaultCacheConfiguration(ClassLoader classLoader) {
            return RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(RedisSerializationContext
                    .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        }
    }

    /**
     * Ehcache缓存配置
     */
    @ConditionalOnClass(name = "org.ehcache.core.EhcacheManager")
    public static class Ehcache3Configuration{
        /**
         * 如果redis缓存存在则使用redis缓存
         * @return
         */
        @Bean({"cacheManager", "ehcache3CacheManager"})
        @ConditionalOnMissingBean(name = "cacheManager")
        public CacheManager ehcache3CacheManager(ObjectProvider<CacheEntry>  caches) throws URISyntaxException {

            final ClassLoader classLoader = getClass().getClassLoader();

            CachingProvider cachingProvider = Caching.getCachingProvider(
                    "org.ehcache.jsr107.EhcacheCachingProvider", classLoader);

            // 通过配置文件创建缓存
            javax.cache.CacheManager cacheManager;
            final URL resource = classLoader.getResource("ehcache.xml");
            if(null != resource) {
                cacheManager = cachingProvider.getCacheManager(resource.toURI(), classLoader);
            } else {
                cacheManager = cachingProvider.getCacheManager();
            }

            // 初始化缓存
            caches.forEach(item -> {
                cacheManager.createCache(item.getName(), doCreateCache(item));
            });

            return new JCacheCacheManager(cacheManager);
        }

        private javax.cache.configuration.Configuration doCreateCache(CacheEntry item) {

            CacheConfiguration configuration = this.authCacheConfiguration(item);
            return Eh107Configuration.fromEhcacheCacheConfiguration(configuration);
        }

        public CacheConfiguration authCacheConfiguration(CacheEntry item) {

            // ResourcePoolsBuilder 用于构建Cache的各项参数
            ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();

            // ResourcePools 存放Cache各项参数的对象
            // heap() 缓存在内存中的大小,还有其他方法读者自行查看
            ResourcePools resourcePools = resourcePoolsBuilder
                    .heap(500, MemoryUnit.MB).build();

            Duration timeToLiveSeconds = Duration.ofSeconds(item.getExpire());

            // String.class, Object.class Cache存放key和value的数据类型
            // withExpiry() 设置信息在缓存中的存放的时间，有两种：<br>
            // 1、TTL缓存自创建之时起至失效时的时间间隔；
            // 2、TTI缓存创建以后，最后一次访问缓存之时至失效之时的时间间隔
            return CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(String.class, item.getClazz(), resourcePools)
                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(timeToLiveSeconds))
                    .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(timeToLiveSeconds))
                    .build();
        }
    }

}
