package com.iteaj.framework.security;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class JwtRevocationService {

    private static final String REVOKED_JTI_PREFIX = "iboot:security:revoked:jti:";
    private static final String REVOKED_REFRESH_PREFIX = "iboot:security:revoked:refresh:";

    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public JwtRevocationService(ObjectProvider<RedisTemplate> redisTemplateProvider) {
        this.redisTemplate = (RedisTemplate<String, Object>) redisTemplateProvider.getIfAvailable();
    }

    public void revokeJti(String jti, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.isBlank(jti) || ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(REVOKED_JTI_PREFIX + jti, Boolean.TRUE, Duration.ofSeconds(ttlSeconds));
    }

    public boolean isJtiRevoked(String jti) {
        if (redisTemplate == null || StrUtil.isBlank(jti)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_JTI_PREFIX + jti));
    }

    public void revokeRefreshToken(String refreshToken, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.isBlank(refreshToken) || ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(REVOKED_REFRESH_PREFIX + refreshToken, Boolean.TRUE, Duration.ofSeconds(ttlSeconds));
    }

    public boolean isRefreshTokenRevoked(String refreshToken) {
        if (redisTemplate == null || StrUtil.isBlank(refreshToken)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_REFRESH_PREFIX + refreshToken));
    }
}
