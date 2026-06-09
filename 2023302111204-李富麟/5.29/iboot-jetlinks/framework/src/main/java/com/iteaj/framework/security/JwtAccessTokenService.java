package com.iteaj.framework.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAccessTokenService {

    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_ACCOUNT = "account";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "authorities";
    public static final String CLAIM_JTI = "jti";
    public static final String CLAIM_ISS = "iss";
    public static final String CLAIM_IAT = "iat";
    public static final String CLAIM_EXP = "exp";
    public static final String CLAIM_CLIENT_ID = "client_id";
    public static final String CLAIM_SCOPE = "scope";
    public static final String CLAIM_AUD = "aud";

    private final FrameworkProperties.Security security;
    private final JwtRevocationService revocationService;

    public JwtAccessTokenService(FrameworkProperties properties, JwtRevocationService revocationService) {
        this.security = properties.getSecurity();
        this.revocationService = revocationService;
    }

    public String createToken(Long userId, AuthenticationUser user, String displayName,
                              List<String> roles, List<String> permissions) {
        return createToken(userId, user, displayName, roles, permissions, null, null, null);
    }

    public String createToken(Long userId, AuthenticationUser user, String displayName,
                              List<String> roles, List<String> permissions,
                              String clientId, List<String> scopes, String audience) {
        Date now = new Date();
        Date exp = DateUtil.offsetSecond(now, (int) security.getAccessTokenTtl());
        Map<String, Object> payload = new LinkedHashMap<>();
        String issuer = security.getOauth2Issuer() == null || security.getOauth2Issuer().trim().isEmpty()
                ? security.getIssuer() : security.getOauth2Issuer();

        payload.put(CLAIM_SUB, String.valueOf(userId));
        payload.put(CLAIM_UID, userId);
        payload.put(CLAIM_ACCOUNT, user.getAccount());
        payload.put(CLAIM_NAME, displayName);
        payload.put(CLAIM_ROLES, roles);
        payload.put(CLAIM_PERMISSIONS, permissions);
        payload.put(CLAIM_JTI, IdUtil.fastSimpleUUID());
        payload.put(CLAIM_ISS, issuer);
        payload.put(CLAIM_IAT, now.getTime() / 1000);
        payload.put(CLAIM_EXP, exp.getTime() / 1000);

        if (clientId != null && !clientId.trim().isEmpty()) {
            payload.put(CLAIM_CLIENT_ID, clientId);
        }

        if (scopes != null && !scopes.isEmpty()) {
            payload.put(CLAIM_SCOPE, String.join(" ", new LinkedHashSet<>(scopes)));
        }

        if (audience != null && !audience.trim().isEmpty()) {
            payload.put(CLAIM_AUD, audience);
        }

        return JWTUtil.createToken(payload, getSecretBytes());
    }

    public JWT parse(String token) {
        return parse(token, null);
    }

    public JWT parse(String token, String expectedClientId) {
        if (token == null || token.trim().isEmpty()) {
            throw SecurityException.unauthorized("缺少访问令牌");
        }

        JWT jwt = JWTUtil.parseToken(token);
        if (!jwt.setKey(getSecretBytes()).verify()) {
            throw SecurityException.unauthorized("访问令牌签名无效");
        }

        Object expPayload = jwt.getPayload(CLAIM_EXP);
        long exp = expPayload != null ? Long.parseLong(String.valueOf(expPayload)) : 0L;
        long now = System.currentTimeMillis() / 1000;
        if (exp <= 0 || exp < now) {
            throw SecurityException.unauthorized("访问令牌已过期");
        }

        Object iatPayload = jwt.getPayload(CLAIM_IAT);
        long iat = iatPayload != null ? Long.parseLong(String.valueOf(iatPayload)) : 0L;
        if (iat <= 0 || iat > now + 60) {
            throw SecurityException.unauthorized("访问令牌签发时间无效");
        }

        String expectedIssuer = security.getOauth2Issuer() == null || security.getOauth2Issuer().trim().isEmpty()
                ? security.getIssuer() : security.getOauth2Issuer();
        String issuer = stringOrNull(jwt.getPayload(CLAIM_ISS));
        if (expectedIssuer != null && !expectedIssuer.isEmpty() && !expectedIssuer.equals(issuer)) {
            throw SecurityException.unauthorized("访问令牌签发者无效");
        }

        String jti = stringOrNull(jwt.getPayload(CLAIM_JTI));
        if (jti == null) {
            throw SecurityException.unauthorized("访问令牌缺少 jti");
        }

        if (revocationService != null && revocationService.isJtiRevoked(jti)) {
            throw SecurityException.unauthorized("访问令牌已被撤销");
        }

        if (expectedClientId != null && !expectedClientId.trim().isEmpty()) {
            String actualClientId = stringOrNull(jwt.getPayload(CLAIM_CLIENT_ID));
            if (!expectedClientId.equals(actualClientId)) {
                throw SecurityException.unauthorized("访问令牌 client_id 无效");
            }
        }

        return jwt;
    }

    @SuppressWarnings("unchecked")
    public JwtTokenClaims resolveClaims(String token) {
        JWT jwt = parse(token);
        JwtTokenClaims claims = new JwtTokenClaims();
        claims.setUserId(Long.valueOf(String.valueOf(jwt.getPayload(CLAIM_UID))));
        claims.setAccount(stringOrNull(jwt.getPayload(CLAIM_ACCOUNT)));
        claims.setDisplayName(stringOrNull(jwt.getPayload(CLAIM_NAME)));
        claims.setClientId(stringOrNull(jwt.getPayload(CLAIM_CLIENT_ID)));
        claims.setIssuer(stringOrNull(jwt.getPayload(CLAIM_ISS)));
        claims.setAudience(stringOrNull(jwt.getPayload(CLAIM_AUD)));
        claims.setJti(stringOrNull(jwt.getPayload(CLAIM_JTI)));
        claims.setIssuedAt(Long.parseLong(String.valueOf(jwt.getPayload(CLAIM_IAT))));
        claims.setExpiresAt(Long.parseLong(String.valueOf(jwt.getPayload(CLAIM_EXP))));

        Object roles = jwt.getPayload(CLAIM_ROLES);
        if (roles instanceof List) {
            claims.setRoles((List<String>) roles);
        }

        Object permissions = jwt.getPayload(CLAIM_PERMISSIONS);
        if (permissions instanceof List) {
            claims.setPermissions((List<String>) permissions);
        }

        Object scopePayload = jwt.getPayload(CLAIM_SCOPE);
        if (scopePayload != null) {
            Set<String> scopes = new LinkedHashSet<>();
            String raw = String.valueOf(scopePayload).trim();
            if (!raw.isEmpty()) {
                for (String item : raw.split("[,\\s]+")) {
                    if (!item.trim().isEmpty()) {
                        scopes.add(item.trim());
                    }
                }
            }
            claims.setScopes(scopes.stream().collect(Collectors.toList()));
        }

        return claims;
    }

    public void revokeByJti(String jti, long ttlSeconds) {
        if (revocationService != null) {
            revocationService.revokeJti(jti, ttlSeconds);
        }
    }

    private String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text == null || text.trim().isEmpty() || "null".equalsIgnoreCase(text) ? null : text;
    }

    private byte[] getSecretBytes() {
        String secret = security.getJwtSecret();
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException("JWT 密钥未配置或长度不足");
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
