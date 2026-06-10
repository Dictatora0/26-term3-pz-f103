package com.iteaj.iboot.plugin.oauth2.sa;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import cn.dev33.satoken.oauth2.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.model.CodeModel;
import cn.dev33.satoken.oauth2.model.RefreshTokenModel;
import cn.dev33.satoken.oauth2.model.RequestAuthModel;
import cn.dev33.satoken.oauth2.model.SaClientModel;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.JwtAccessTokenService;
import com.iteaj.framework.security.JwtTokenClaims;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2SecurityStore;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SaOAuth2TemplateImpl extends SaOAuth2Template {

    private static final long PKCE_TTL_SECONDS = 300L;

    private final Oauth2AppService oauth2AppService;
    private final Oauth2UserService oauth2UserService;
    private final Oauth2SecurityStore oauth2SecurityStore;
    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final FrameworkProperties properties;

    public SaOAuth2TemplateImpl(Oauth2AppService oauth2AppService,
                                Oauth2UserService oauth2UserService,
                                Oauth2SecurityStore oauth2SecurityStore,
                                AuthenticationService authenticationService,
                                AuthorizationService authorizationService,
                                JwtAccessTokenService jwtAccessTokenService,
                                FrameworkProperties properties) {
        this.oauth2AppService = oauth2AppService;
        this.oauth2UserService = oauth2UserService;
        this.oauth2SecurityStore = oauth2SecurityStore;
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.jwtAccessTokenService = jwtAccessTokenService;
        this.properties = properties;
    }

    @Override
    public SaClientModel getClientModel(String clientId) {
        return oauth2AppService.getByClientId(clientId).ofNullable().map(item -> {
            SaClientModel clientModel = new SaClientModel();
            BeanUtil.copyProperties(item, clientModel);
            clientModel.setIsAutoMode(properties.getSecurity().isOauth2AutoApprove());
            clientModel.setIsCode(true);
            clientModel.setIsImplicit(false);
            clientModel.setIsPassword(false);
            clientModel.setIsClient(true);
            clientModel.setIsNewRefresh(true);
            return clientModel;
        }).orElse(null);
    }

    @Override
    public String getOpenid(String clientId, Object loginId) {
        return oauth2UserService.getByClientIdAndLoginId(clientId, loginId)
                .ofNullable()
                .map(item -> item.getOpenid())
                .orElse(String.valueOf(loginId));
    }

    @Override
    public CodeModel generateCode(RequestAuthModel requestAuthModel) {
        CodeModel codeModel = super.generateCode(requestAuthModel);
        String codeChallenge = SaHolder.getRequest().getParam("code_challenge");
        if (StrUtil.isBlank(codeChallenge)) {
            return codeModel;
        }

        String codeChallengeMethod = SaHolder.getRequest().getParam("code_challenge_method", "S256");
        oauth2SecurityStore.savePkce(
                codeModel.code,
                requestAuthModel.clientId,
                codeChallenge,
                codeChallengeMethod,
                PKCE_TTL_SECONDS
        );
        return codeModel;
    }

    @Override
    public AccessTokenModel generateAccessToken(String code) {
        AccessTokenModel accessTokenModel = super.generateAccessToken(code);
        bindJwt(accessTokenModel);
        return accessTokenModel;
    }

    @Override
    public AccessTokenModel refreshAccessToken(String refreshToken) {
        if (oauth2SecurityStore.isRefreshTokenRevoked(refreshToken)) {
            throw new IllegalStateException("refresh_token has been revoked");
        }

        Map<String, String> oldBinding = oauth2SecurityStore.getRefreshBinding(refreshToken);
        if (!oldBinding.isEmpty()) {
            revokeJwtBinding(oldBinding, 300);
        }

        AccessTokenModel model = super.refreshAccessToken(refreshToken);
        bindJwt(model);
        return model;
    }

    @Override
    public void revokeAccessToken(String accessToken) {
        String jwtAccessToken = oauth2SecurityStore.getJwtAccessToken(accessToken);
        if (StrUtil.isNotBlank(jwtAccessToken)) {
            JwtTokenClaims claims = jwtAccessTokenService.resolveClaims(jwtAccessToken);
            long ttl = Math.max(claims.getExpiresAt() - (System.currentTimeMillis() / 1000), 60);
            jwtAccessTokenService.revokeByJti(claims.getJti(), ttl);
        }

        AccessTokenModel accessTokenModel = getAccessToken(accessToken);
        if (accessTokenModel != null && StrUtil.isNotBlank(accessTokenModel.refreshToken)) {
            RefreshTokenModel refreshTokenModel = getRefreshToken(accessTokenModel.refreshToken);
            long refreshTtl = refreshTokenModel != null
                    ? Math.max(refreshTokenModel.expiresTime - (System.currentTimeMillis() / 1000), 60)
                    : 300;
            oauth2SecurityStore.revokeRefreshToken(accessTokenModel.refreshToken, refreshTtl);
            oauth2SecurityStore.deleteRefreshBinding(accessTokenModel.refreshToken);
        }

        oauth2SecurityStore.deleteJwtAccessToken(accessToken);
        super.revokeAccessToken(accessToken);
    }

    private void bindJwt(AccessTokenModel accessTokenModel) {
        AuthenticationUser user = authenticationService.getById(accessTokenModel.loginId);
        if (user == null) {
            return;
        }

        Long userId = Long.valueOf(String.valueOf(accessTokenModel.loginId));
        List<String> roles = authorizationService.getRoles(userId);
        List<String> permissions = authorizationService.getPermissions(userId);
        List<String> scopes = oauth2SecurityStore.parseScope(accessTokenModel.scope);
        List<String> allowedScopes = resolveAllowedScopes(userId);
        if (!scopes.isEmpty() && !allowedScopes.isEmpty()) {
            scopes = scopes.stream()
                    .filter(allowedScopes::contains)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        } else if (allowedScopes.isEmpty()) {
            scopes = Collections.emptyList();
        }

        String jwtAccessToken = jwtAccessTokenService.createToken(
                userId,
                user,
                user.getAccount(),
                roles != null ? roles : Collections.emptyList(),
                permissions != null ? permissions : Collections.emptyList(),
                accessTokenModel.clientId,
                scopes,
                accessTokenModel.clientId
        );

        JwtTokenClaims claims = jwtAccessTokenService.resolveClaims(jwtAccessToken);
        long accessTtl = Math.max(accessTokenModel.getExpiresIn(), 60);
        long refreshTtl = Math.max(accessTokenModel.getRefreshExpiresIn(), 60);

        oauth2SecurityStore.saveJwtAccessToken(accessTokenModel.accessToken, jwtAccessToken, accessTtl);
        if (StrUtil.isNotBlank(accessTokenModel.refreshToken)) {
            oauth2SecurityStore.saveRefreshBinding(accessTokenModel.refreshToken, jwtAccessToken, claims.getJti(), refreshTtl);
        }
    }

    private List<String> resolveAllowedScopes(Object loginId) {
        if (loginId == null) {
            return Collections.emptyList();
        }

        Long userId = Long.valueOf(String.valueOf(loginId));
        if (userId == 1L) {
            return java.util.Arrays.asList("device.read", "device.control", "user.manage");
        }

        List<String> permissions = authorizationService.getPermissions(userId);
        java.util.LinkedHashSet<String> scopes = new java.util.LinkedHashSet<>();
        if (permissions != null) {
            if (permissions.contains("iot:device:view")) {
                scopes.add("device.read");
            }
            if (permissions.contains("iot:device:ctrl")) {
                scopes.add("device.control");
            }
            for (String permission : permissions) {
                if (permission != null && (permission.startsWith("core:admin:")
                        || permission.startsWith("core:role:")
                        || permission.startsWith("core:menu:")
                        || permission.startsWith("core:online:"))) {
                    scopes.add("user.manage");
                    break;
                }
            }
        }

        return new java.util.ArrayList<>(scopes);
    }

    private void revokeJwtBinding(Map<String, String> binding, long fallbackTtlSeconds) {
        String jwtAccessToken = binding.get("jwtAccessToken");
        String jwtJti = binding.get("jwtJti");
        if (StrUtil.isBlank(jwtAccessToken) || StrUtil.isBlank(jwtJti)) {
            return;
        }

        try {
            JwtTokenClaims claims = jwtAccessTokenService.resolveClaims(jwtAccessToken);
            long ttl = Math.max(claims.getExpiresAt() - (System.currentTimeMillis() / 1000), fallbackTtlSeconds);
            jwtAccessTokenService.revokeByJti(jwtJti, ttl);
        } catch (Exception ignore) {
            jwtAccessTokenService.revokeByJti(jwtJti, fallbackTtlSeconds);
        }
    }
}
