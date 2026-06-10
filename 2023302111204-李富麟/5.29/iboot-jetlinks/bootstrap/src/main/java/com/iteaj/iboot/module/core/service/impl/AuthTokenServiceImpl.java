package com.iteaj.iboot.module.core.service.impl;

import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.JwtAccessTokenService;
import com.iteaj.framework.security.SecurityToken;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import com.iteaj.iboot.module.core.dto.AuthTokenDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.AuthTokenService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

@Service
public class AuthTokenServiceImpl implements AuthTokenService {

    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final com.iteaj.framework.autoconfigure.FrameworkProperties properties;

    public AuthTokenServiceImpl(AuthenticationService authenticationService, AuthorizationService authorizationService
            , JwtAccessTokenService jwtAccessTokenService
            , com.iteaj.framework.autoconfigure.FrameworkProperties properties) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.jwtAccessTokenService = jwtAccessTokenService;
        this.properties = properties;
    }

    @Override
    public AuthTokenDto issueToken(SecurityToken token, HttpServletRequest request, HttpServletResponse response) {
        SecurityUtil.login(token, request, response);

        AuthenticationUser user = authenticationService.getByAccount(token.getAccount());
        Admin admin = (Admin) user;
        List<String> roles = authorizationService.getRoles(admin.getId());
        List<String> permissions = authorizationService.getPermissions(admin.getId());
        String accessToken = jwtAccessTokenService.createToken(admin.getId(), user, admin.getName()
                , roles != null ? roles : Collections.emptyList()
                , permissions != null ? permissions : Collections.emptyList());

        response.setHeader(properties.getWeb().getSession().getTokenName(), accessToken);

        return new AuthTokenDto()
                .setAccessToken(accessToken)
                .setExpiresIn(properties.getSecurity().getAccessTokenTtl())
                .setUserId(admin.getId())
                .setUsername(admin.getAccount())
                .setDisplayName(admin.getName())
                .setRoles(roles != null ? roles : Collections.emptyList());
    }
}
