package com.iteaj.iboot.plugin.oauth2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2SecurityStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RedisOauth2SecurityStore implements Oauth2SecurityStore {

    private static final String PREFIX = "iboot:oauth2:";
    private static final String PKCE_PREFIX = PREFIX + "pkce:";
    private static final String JWT_ACCESS_PREFIX = PREFIX + "jwt-access:";
    private static final String JWT_ACCESS_REVERSE_PREFIX = PREFIX + "jwt-access-reverse:";
    private static final String REFRESH_BIND_PREFIX = PREFIX + "refresh-bind:";
    private static final String REFRESH_REVOKE_PREFIX = PREFIX + "refresh-revoke:";
    private static final String ACCESS_REVOKE_PREFIX = PREFIX + "access-revoke:";
    private static final String STATE_PREFIX = PREFIX + "state:";

    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public RedisOauth2SecurityStore(ObjectProvider<RedisTemplate> redisTemplateProvider) {
        this.redisTemplate = (RedisTemplate<String, Object>) redisTemplateProvider.getIfAvailable();
    }

    @Override
    public void savePkce(String code, String clientId, String codeChallenge, String codeChallengeMethod, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.hasBlank(code, clientId, codeChallenge)) {
            return;
        }
        Map<String, String> value = new LinkedHashMap<>();
        value.put("clientId", clientId);
        value.put("codeChallenge", codeChallenge);
        value.put("codeChallengeMethod", codeChallengeMethod);
        redisTemplate.opsForValue().set(PKCE_PREFIX + code, value, Duration.ofSeconds(Math.max(ttlSeconds, 60)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getPkce(String code) {
        if (redisTemplate == null || StrUtil.isBlank(code)) {
            return Collections.emptyMap();
        }
        Object value = redisTemplate.opsForValue().get(PKCE_PREFIX + code);
        return value instanceof Map ? (Map<String, String>) value : Collections.emptyMap();
    }

    @Override
    public void deletePkce(String code) {
        if (redisTemplate != null && StrUtil.isNotBlank(code)) {
            redisTemplate.delete(PKCE_PREFIX + code);
        }
    }

    @Override
    public void saveJwtAccessToken(String oauthAccessToken, String jwtAccessToken, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.hasBlank(oauthAccessToken, jwtAccessToken)) {
            return;
        }
        Duration duration = Duration.ofSeconds(Math.max(ttlSeconds, 60));
        redisTemplate.opsForValue().set(JWT_ACCESS_PREFIX + oauthAccessToken, jwtAccessToken, duration);
        redisTemplate.opsForValue().set(JWT_ACCESS_REVERSE_PREFIX + jwtAccessToken, oauthAccessToken, duration);
    }

    @Override
    public String getJwtAccessToken(String oauthAccessToken) {
        if (redisTemplate == null || StrUtil.isBlank(oauthAccessToken)) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(JWT_ACCESS_PREFIX + oauthAccessToken);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public String getOauthAccessTokenByJwt(String jwtAccessToken) {
        if (redisTemplate == null || StrUtil.isBlank(jwtAccessToken)) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(JWT_ACCESS_REVERSE_PREFIX + jwtAccessToken);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public void deleteJwtAccessToken(String oauthAccessToken) {
        if (redisTemplate != null && StrUtil.isNotBlank(oauthAccessToken)) {
            String jwtToken = getJwtAccessToken(oauthAccessToken);
            redisTemplate.delete(JWT_ACCESS_PREFIX + oauthAccessToken);
            if (StrUtil.isNotBlank(jwtToken)) {
                redisTemplate.delete(JWT_ACCESS_REVERSE_PREFIX + jwtToken);
            }
        }
    }

    @Override
    public void saveRefreshBinding(String refreshToken, String jwtAccessToken, String jwtJti, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.hasBlank(refreshToken, jwtAccessToken, jwtJti)) {
            return;
        }
        Map<String, String> value = new LinkedHashMap<>();
        value.put("jwtAccessToken", jwtAccessToken);
        value.put("jwtJti", jwtJti);
        redisTemplate.opsForValue().set(REFRESH_BIND_PREFIX + refreshToken, value, Duration.ofSeconds(Math.max(ttlSeconds, 60)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getRefreshBinding(String refreshToken) {
        if (redisTemplate == null || StrUtil.isBlank(refreshToken)) {
            return Collections.emptyMap();
        }
        Object value = redisTemplate.opsForValue().get(REFRESH_BIND_PREFIX + refreshToken);
        return value instanceof Map ? (Map<String, String>) value : Collections.emptyMap();
    }

    @Override
    public void deleteRefreshBinding(String refreshToken) {
        if (redisTemplate != null && StrUtil.isNotBlank(refreshToken)) {
            redisTemplate.delete(REFRESH_BIND_PREFIX + refreshToken);
        }
    }

    @Override
    public void revokeRefreshToken(String refreshToken, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.isBlank(refreshToken)) {
            return;
        }
        redisTemplate.opsForValue().set(REFRESH_REVOKE_PREFIX + refreshToken, Boolean.TRUE, Duration.ofSeconds(Math.max(ttlSeconds, 60)));
    }

    @Override
    public boolean isRefreshTokenRevoked(String refreshToken) {
        return redisTemplate != null
                && StrUtil.isNotBlank(refreshToken)
                && Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_REVOKE_PREFIX + refreshToken));
    }

    @Override
    public void revokeAccessJti(String jti, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.isBlank(jti)) {
            return;
        }
        redisTemplate.opsForValue().set(ACCESS_REVOKE_PREFIX + jti, Boolean.TRUE, Duration.ofSeconds(Math.max(ttlSeconds, 60)));
    }

    @Override
    public boolean isAccessJtiRevoked(String jti) {
        return redisTemplate != null
                && StrUtil.isNotBlank(jti)
                && Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_REVOKE_PREFIX + jti));
    }

    @Override
    public void saveAuthorizationState(String state, String redirectUri, long ttlSeconds) {
        if (redisTemplate == null || StrUtil.hasBlank(state, redirectUri)) {
            return;
        }
        redisTemplate.opsForValue().set(STATE_PREFIX + state, redirectUri, Duration.ofSeconds(Math.max(ttlSeconds, 60)));
    }

    @Override
    public String getAuthorizationState(String state) {
        if (redisTemplate == null || StrUtil.isBlank(state)) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(STATE_PREFIX + state);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public void deleteAuthorizationState(String state) {
        if (redisTemplate != null && StrUtil.isNotBlank(state)) {
            redisTemplate.delete(STATE_PREFIX + state);
        }
    }

    @Override
    public List<String> parseScope(String scope) {
        if (StrUtil.isBlank(scope)) {
            return Collections.emptyList();
        }
        return Arrays.stream(scope.split("[,\\s]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }
}
