package com.iteaj.framework.cache;

import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Function;

public class HashRedisCacheWriter implements RedisCacheWriter {

    private final RedisConnectionFactory connectionFactory;
    private final Duration sleepTime;
    private final CacheStatisticsCollector statistics;
    private final BatchStrategy batchStrategy;

    /**
     * @param connectionFactory must not be {@literal null}.
     * @param batchStrategy must not be {@literal null}.
     */
    public HashRedisCacheWriter(RedisConnectionFactory connectionFactory, BatchStrategy batchStrategy) {
        this(connectionFactory, Duration.ZERO, CacheStatisticsCollector.none(), batchStrategy);
    }

    public HashRedisCacheWriter(RedisConnectionFactory connectionFactory, Duration sleepTime,
                            CacheStatisticsCollector cacheStatisticsCollector, BatchStrategy batchStrategy) {

        Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
        Assert.notNull(sleepTime, "SleepTime must not be null!");
        Assert.notNull(cacheStatisticsCollector, "CacheStatisticsCollector must not be null!");
        Assert.notNull(batchStrategy, "BatchStrategy must not be null!");

        this.connectionFactory = connectionFactory;
        this.sleepTime = sleepTime;
        this.statistics = cacheStatisticsCollector;
        this.batchStrategy = batchStrategy;
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        execute(name, connection -> {
            connection.hSet(name.getBytes(StandardCharsets.UTF_8), key, value);
            return null;
        });
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return execute(name, connection -> connection.hGet(name.getBytes(StandardCharsets.UTF_8), key));
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void remove(String name, byte[] key) {
        execute(name, connection -> connection.hDel(name.getBytes(StandardCharsets.UTF_8), key));
    }

    @Override
    public void clean(String name, byte[] pattern) {
        execute(name, connection -> batchStrategy.cleanCache(connection, name, pattern));
    }

    private <T> T execute(String name, Function<RedisConnection, T> callback) {

        RedisConnection connection = connectionFactory.getConnection();
        try {

            return callback.apply(connection);
        } finally {
            connection.close();
        }
    }


    @Override
    public void clearStatistics(String name) {
        statistics.reset(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return new HashRedisCacheWriter(connectionFactory, sleepTime, cacheStatisticsCollector, this.batchStrategy);
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return statistics.getCacheStatistics(cacheName);
    }
}
