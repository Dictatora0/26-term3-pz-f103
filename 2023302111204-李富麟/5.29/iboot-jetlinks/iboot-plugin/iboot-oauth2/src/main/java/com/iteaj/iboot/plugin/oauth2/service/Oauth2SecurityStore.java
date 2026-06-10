package com.iteaj.iboot.plugin.oauth2.service;

import java.util.List;
import java.util.Map;

public interface Oauth2SecurityStore {

    void savePkce(String code, String clientId, String codeChallenge, String codeChallengeMethod, long ttlSeconds);

    Map<String, String> getPkce(String code);

    void deletePkce(String code);

    void saveJwtAccessToken(String oauthAccessToken, String jwtAccessToken, long ttlSeconds);

    String getJwtAccessToken(String oauthAccessToken);

    String getOauthAccessTokenByJwt(String jwtAccessToken);

    void deleteJwtAccessToken(String oauthAccessToken);

    void saveRefreshBinding(String refreshToken, String jwtAccessToken, String jwtJti, long ttlSeconds);

    Map<String, String> getRefreshBinding(String refreshToken);

    void deleteRefreshBinding(String refreshToken);

    void revokeRefreshToken(String refreshToken, long ttlSeconds);

    boolean isRefreshTokenRevoked(String refreshToken);

    void revokeAccessJti(String jti, long ttlSeconds);

    boolean isAccessJtiRevoked(String jti);

    void saveAuthorizationState(String state, String redirectUri, long ttlSeconds);

    String getAuthorizationState(String state);

    void deleteAuthorizationState(String state);

    List<String> parseScope(String scope);
}
