package com.iteaj.iboot.plugin.oauth2.controller;

import cn.dev33.satoken.oauth2.config.SaOAuth2Config;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Handle;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Util;
import cn.dev33.satoken.oauth2.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.model.CodeModel;
import cn.dev33.satoken.oauth2.model.RefreshTokenModel;
import cn.dev33.satoken.oauth2.model.RequestAuthModel;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.PasswordCodec;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2App;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2SecurityStore;
import com.iteaj.framework.security.AuthenticationService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RequestMapping
@RestController
public class SaOAuth2ServerController {

    private final SaOAuth2Config saOAuth2Config;
    private final SaOAuth2Template saOAuth2Template;
    private final Oauth2AppService oauth2AppService;
    private final Oauth2SecurityStore oauth2SecurityStore;
    private final FrameworkProperties properties;
    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;

    public SaOAuth2ServerController(SaOAuth2Config saOAuth2Config,
                                    SaOAuth2Template saOAuth2Template,
                                    Oauth2AppService oauth2AppService,
                                    Oauth2SecurityStore oauth2SecurityStore,
                                    FrameworkProperties properties,
                                    AuthenticationService authenticationService,
                                    AuthorizationService authorizationService) {
        this.saOAuth2Config = saOAuth2Config;
        this.saOAuth2Template = saOAuth2Template;
        this.oauth2AppService = oauth2AppService;
        this.oauth2SecurityStore = oauth2SecurityStore;
        this.properties = properties;
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/oauth2/authorize")
    public Object authorize(HttpServletRequest request,
                            @RequestParam("response_type") String responseType,
                            @RequestParam("client_id") String clientId,
                            @RequestParam("redirect_uri") String redirectUri,
                            @RequestParam(value = "scope", required = false, defaultValue = "") String scope,
                            @RequestParam(value = "state", required = false) String state,
                            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
                            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {
        Oauth2App client = oauth2AppService.getByClientId(clientId)
                .ifNotPresentThrow("invalid client_id")
                .getData();

        enforcePkce(client, codeChallenge, codeChallengeMethod);
        if (!StpUtil.isLogin()) {
            return saOAuth2Config.getNotLoginView().get();
        }

        String normalizedScope = normalizeScope(scope, StpUtil.getLoginId());
        RequestAuthModel requestAuthModel = new RequestAuthModel()
                .setResponseType(responseType)
                .setClientId(clientId)
                .setRedirectUri(redirectUri)
                .setScope(normalizedScope)
                .setState(state)
                .setLoginId(StpUtil.getLoginId())
                .checkModel();

        SaOAuth2Util.checkRightUrl(requestAuthModel.clientId, requestAuthModel.redirectUri);
        SaOAuth2Util.checkContract(requestAuthModel.clientId, requestAuthModel.scope);

        boolean isGrant = SaOAuth2Util.isGrant(requestAuthModel.loginId, requestAuthModel.clientId, requestAuthModel.scope);
        if (!isGrant) {
            if (!Boolean.TRUE.equals(client.getRequirePkce()) && !properties.getSecurity().isOauth2AutoApprove()) {
                return saOAuth2Config.getConfirmView().apply(requestAuthModel.clientId, requestAuthModel.scope);
            }
            SaOAuth2Util.saveGrantScope(requestAuthModel.clientId, requestAuthModel.loginId, requestAuthModel.scope);
        }

        if ("code".equalsIgnoreCase(requestAuthModel.responseType)) {
            CodeModel codeModel = saOAuth2Template.generateCode(requestAuthModel);
            return cn.dev33.satoken.context.SaHolder.getResponse()
                    .redirect(SaOAuth2Util.buildRedirectUri(requestAuthModel.redirectUri, codeModel.code, requestAuthModel.state));
        }

        if ("token".equalsIgnoreCase(requestAuthModel.responseType)) {
            AccessTokenModel accessTokenModel = saOAuth2Template.generateAccessToken(requestAuthModel, false);
            return cn.dev33.satoken.context.SaHolder.getResponse()
                    .redirect(SaOAuth2Util.buildImplicitRedirectUri(requestAuthModel.redirectUri, accessTokenModel.accessToken, requestAuthModel.state));
        }

        throw SecurityException.unauthorized("unsupported response_type");
    }

    @PostMapping("/oauth2/token")
    public SaResult token(@RequestParam("grant_type") String grantType,
                          @RequestParam("client_id") String clientId,
                          @RequestParam(value = "client_secret", required = false) String clientSecret,
                          @RequestParam(value = "code", required = false) String code,
                          @RequestParam(value = "redirect_uri", required = false) String redirectUri,
                          @RequestParam(value = "refresh_token", required = false) String refreshToken,
                          @RequestParam(value = "code_verifier", required = false) String codeVerifier) {
        Oauth2App client = oauth2AppService.getByClientId(clientId)
                .ifNotPresentThrow("invalid client_id")
                .getData();

        validateClientSecret(client, clientSecret);

        if ("authorization_code".equals(grantType)) {
            validatePkceVerifier(code, clientId, codeVerifier);
            AccessTokenModel tokenModel = exchangeAuthorizationCode(clientId, code, redirectUri);
            return replaceAccessToken(SaResult.data(tokenModel.toLineMap()));
        }

        if ("refresh_token".equals(grantType)) {
            if (oauth2SecurityStore.isRefreshTokenRevoked(refreshToken)) {
                throw SecurityException.unauthorized("refresh_token revoked");
            }
            validateRefreshToken(clientId, refreshToken);
            AccessTokenModel tokenModel = saOAuth2Template.refreshAccessToken(refreshToken);
            return replaceAccessToken(SaResult.data(tokenModel.toLineMap()));
        }

        throw SecurityException.unauthorized("unsupported grant_type");
    }

    @PostMapping("/oauth2/revoke")
    public SaResult revoke(@RequestParam("client_id") String clientId,
                           @RequestParam(value = "client_secret", required = false) String clientSecret,
                           @RequestParam(value = "token", required = false) String token,
                           @RequestParam(value = "access_token", required = false) String accessToken) {
        Oauth2App client = oauth2AppService.getByClientId(clientId)
                .ifNotPresentThrow("invalid client_id")
                .getData();
        validateClientSecret(client, clientSecret);

        String tokenValue = StrUtil.isNotBlank(token) ? token : accessToken;
        if (StrUtil.isBlank(tokenValue)) {
            throw SecurityException.unauthorized("missing token");
        }

        Map<String, String> refreshBinding = oauth2SecurityStore.getRefreshBinding(tokenValue);
        if (!refreshBinding.isEmpty()) {
            oauth2SecurityStore.revokeRefreshToken(tokenValue, 3600);
            oauth2SecurityStore.deleteRefreshBinding(tokenValue);
            return SaResult.ok("refresh_token revoked");
        }

        String oauthAccessToken = oauth2SecurityStore.getOauthAccessTokenByJwt(tokenValue);
        if (StrUtil.isNotBlank(oauthAccessToken)) {
            saOAuth2Template.revokeAccessToken(oauthAccessToken);
            return SaResult.ok("access_token revoked");
        }

        saOAuth2Template.revokeAccessToken(tokenValue);
        return SaResult.ok("token revoked");
    }

    @PostMapping("/oauth2/doLogin")
    public SaResult doLogin(@RequestParam("name") String name,
                            @RequestParam("pwd") String pwd) {
        AuthenticationUser user = authenticationService.getByAccount(name);
        if (user == null) {
            throw SecurityException.unauthorized("invalid account");
        }
        if (!user.allowLogin()) {
            throw SecurityException.forbidden("account disabled");
        }
        if (!PasswordCodec.matches(pwd, user.getPassword())) {
            throw SecurityException.unauthorized("invalid password");
        }

        Object loginId = user instanceof com.iteaj.framework.Entity
                ? ((com.iteaj.framework.Entity<?>) user).getId()
                : name;
        StpUtil.login(loginId);
        return SaResult.ok("login success");
    }

    @PostMapping("/oauth2/doConfirm")
    public SaResult doConfirm(@RequestParam("client_id") String clientId,
                              @RequestParam(value = "scope", required = false, defaultValue = "") String scope) {
        return (SaResult) SaOAuth2Handle.doConfirm(cn.dev33.satoken.context.SaHolder.getRequest());
    }

    @GetMapping("/oauth2/config")
    public SaResult config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("issuer", properties.getSecurity().getOauth2Issuer());
        data.put("authorizeEndpoint", "/oauth2/authorize");
        data.put("tokenEndpoint", "/oauth2/token");
        data.put("revokeEndpoint", "/oauth2/revoke");
        data.put("defaultClientId", "iboot-local-web");
        data.put("defaultScopes", new String[]{"device.read", "device.control", "user.manage"});
        return SaResult.data(data);
    }

    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }

    private void enforcePkce(Oauth2App client, String codeChallenge, String codeChallengeMethod) {
        boolean requirePkce = client.getRequirePkce() == null || client.getRequirePkce() == 1;
        if (!requirePkce) {
            return;
        }

        if (StrUtil.isBlank(codeChallenge)) {
            throw SecurityException.unauthorized("missing code_challenge");
        }

        if (!"S256".equalsIgnoreCase(StrUtil.blankToDefault(codeChallengeMethod, "S256"))) {
            throw SecurityException.unauthorized("only S256 PKCE is supported");
        }
    }

    private void validatePkceVerifier(String code, String clientId, String codeVerifier) {
        if (StrUtil.isBlank(code)) {
            throw SecurityException.unauthorized("missing authorization code");
        }

        Map<String, String> pkce = oauth2SecurityStore.getPkce(code);
        if (pkce.isEmpty()) {
            return;
        }

        if (!clientId.equals(pkce.get("clientId"))) {
            throw SecurityException.unauthorized("pkce client_id mismatch");
        }

        if (StrUtil.isBlank(codeVerifier)) {
            throw SecurityException.unauthorized("missing code_verifier");
        }

        String expected = pkce.get("codeChallenge");
        String actual = buildCodeChallenge(codeVerifier);
        if (!expected.equals(actual)) {
            throw SecurityException.unauthorized("code_verifier mismatch");
        }

        oauth2SecurityStore.deletePkce(code);
    }

    private String buildCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] value = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        } catch (Exception e) {
            throw new IllegalStateException("PKCE digest failed", e);
        }
    }

    private void validateClientSecret(Oauth2App client, String clientSecret) {
        boolean publicClient = client.getPublicClient() != null && client.getPublicClient() == 1;
        if (publicClient) {
            return;
        }

        if (StrUtil.isBlank(clientSecret)) {
            throw SecurityException.unauthorized("missing client_secret");
        }

        if (PasswordCodec.matches(clientSecret, client.getClientSecret())) {
            return;
        }

        if (!clientSecret.equals(client.getClientSecret())) {
            throw SecurityException.unauthorized("invalid client_secret");
        }
    }

    private AccessTokenModel exchangeAuthorizationCode(String clientId, String code, String redirectUri) {
        CodeModel codeModel = saOAuth2Template.getCode(code);
        if (codeModel == null) {
            throw SecurityException.unauthorized("invalid authorization code");
        }

        if (!StrUtil.equals(clientId, codeModel.getClientId())) {
            throw SecurityException.unauthorized("authorization code client mismatch");
        }

        if (StrUtil.isBlank(redirectUri)) {
            throw SecurityException.unauthorized("missing redirect_uri");
        }

        if (!StrUtil.equals(redirectUri, codeModel.getRedirectUri())) {
            throw SecurityException.unauthorized("redirect_uri mismatch");
        }

        return saOAuth2Template.generateAccessToken(code);
    }

    private void validateRefreshToken(String clientId, String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw SecurityException.unauthorized("missing refresh_token");
        }

        RefreshTokenModel tokenModel = saOAuth2Template.getRefreshToken(refreshToken);
        if (tokenModel == null) {
            throw SecurityException.unauthorized("invalid refresh_token");
        }

        if (!StrUtil.equals(clientId, tokenModel.clientId)) {
            throw SecurityException.unauthorized("refresh_token client mismatch");
        }
    }

    private String normalizeScope(String scope, Object loginId) {
        List<String> requestedScopes = oauth2SecurityStore.parseScope(scope);
        List<String> allowedScopes = resolveAllowedScopes(loginId);

        if (requestedScopes.isEmpty()) {
            return StrUtil.join(",", allowedScopes);
        }

        LinkedHashSet<String> grantedScopes = new LinkedHashSet<>();
        for (String requestedScope : requestedScopes) {
            if (allowedScopes.contains(requestedScope)) {
                grantedScopes.add(requestedScope);
            }
        }

        if (grantedScopes.isEmpty()) {
            throw SecurityException.forbidden("requested scope not allowed");
        }

        return StrUtil.join(",", grantedScopes);
    }

    private List<String> resolveAllowedScopes(Object loginId) {
        if (loginId == null) {
            return new ArrayList<>();
        }

        Long userId = Long.valueOf(String.valueOf(loginId));
        if (userId == 1L) {
            List<String> scopes = new ArrayList<>(3);
            scopes.add("device.read");
            scopes.add("device.control");
            scopes.add("user.manage");
            return scopes;
        }

        List<String> permissions = authorizationService.getPermissions(userId);
        LinkedHashSet<String> allowedScopes = new LinkedHashSet<>();
        if (permissions != null) {
            if (permissions.contains("iot:device:view")) {
                allowedScopes.add("device.read");
            }
            if (permissions.contains("iot:device:ctrl")) {
                allowedScopes.add("device.control");
            }
            if (permissions.stream().anyMatch(this::isUserManagePermission)) {
                allowedScopes.add("user.manage");
            }
        }

        return new ArrayList<>(allowedScopes);
    }

    private boolean isUserManagePermission(String permission) {
        if (StrUtil.isBlank(permission)) {
            return false;
        }

        return permission.startsWith("core:admin:")
                || permission.startsWith("core:role:")
                || permission.startsWith("core:menu:")
                || permission.startsWith("core:online:");
    }

    @SuppressWarnings("unchecked")
    private SaResult replaceAccessToken(SaResult result) {
        if (result == null || !(result.getData() instanceof Map)) {
            return result;
        }

        Map<String, Object> data = (Map<String, Object>) result.getData();
        String oauthAccessToken = data.get("access_token") == null ? null : String.valueOf(data.get("access_token"));
        if (StrUtil.isBlank(oauthAccessToken)) {
            return result;
        }

        String jwtAccessToken = oauth2SecurityStore.getJwtAccessToken(oauthAccessToken);
        if (StrUtil.isBlank(jwtAccessToken)) {
            AccessTokenModel model = saOAuth2Template.getAccessToken(oauthAccessToken);
            if (model != null) {
                jwtAccessToken = oauth2SecurityStore.getJwtAccessToken(model.accessToken);
            }
        }

        if (StrUtil.isNotBlank(jwtAccessToken)) {
            data.put("access_token", jwtAccessToken);
            data.put("token_type", "Bearer");
        }

        return result.setData(data);
    }
}
