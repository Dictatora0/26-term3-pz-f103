package com.iteaj.iboot.plugin.shiro.redis;

import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.security.SecurityUtil;
import lombok.SneakyThrows;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * create time: 2021/7/3
 *
 * @author iteaj
 * @since 1.0
 */
public class ShiroRedisSessionCacheManager implements CacheManager, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private FrameworkProperties properties;

    private ValueOperations valueOperations;
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap();

    @Override
    public Cache getCache(String s) throws CacheException {
        Cache cache = caches.get(s);
        if(cache == null) {
            synchronized (caches) {
                cache = caches.get(s);
                if(cache != null) {
                    return cache;
                }

                cache = new MapCache(s, new HashMap());
                this.caches.put(s, cache);
            }
        }

        return cache;
    }

    protected class RedisSessionCache implements Cache<String, Session>{

        @SneakyThrows
        @Override
        public Session get(String s) throws CacheException {
            // 首先先从线程上下文获取session, 将session和线程绑定
            Object session = SecurityUtil.getRequestAttr(s).orElse(null);
            if(session instanceof Session) {
                return (Session) session;
            }

            session = valueOperations.get(CoreConst.SESSION_KEY_PREFIX +s);
            if(session instanceof Session) {
                SecurityUtil.setRequestAttr(s, session);
                return (Session) session;
            }

            return null;
        }

        @Override
        public Session put(String s, Session session) throws CacheException {
            valueOperations.set(CoreConst.SESSION_KEY_PREFIX+s, session, session.getTimeout(), TimeUnit.MILLISECONDS);
            SecurityUtil.setRequestAttr(s, session);
            return session;
        }

        @Override
        public Session remove(String s) throws CacheException {
            final Session session = this.get(CoreConst.SESSION_KEY_PREFIX+s);
            if(session != null) {
                redisTemplate.delete(CoreConst.SESSION_KEY_PREFIX+s);
            }

            return session;
        }

        @Override
        public void clear() throws CacheException {
            final Set<String> keys = keys();
            if(!keys().isEmpty()) {
                redisTemplate.delete(keys);
            }
        }

        @Override
        public int size() {
            return keys().size();
        }

        @Override
        public Set<String> keys() {
            final Set<String> keys = redisTemplate
                    .keys(CoreConst.SESSION_KEY_PREFIX + "*");

            if(!CollectionUtils.isEmpty(keys)) {
                return keys.stream().map(key -> key
                        .split(":")[1]).collect(Collectors.toSet());
            }

            return Collections.emptySet();
        }

        @Override
        public Collection<Session> values() {
            return Collections.emptyList();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.valueOperations = redisTemplate.opsForValue();
        FrameworkProperties.Session session = this.properties.getWeb().getSession();
        this.caches.put(session.getName(), new RedisSessionCache());
    }
}
