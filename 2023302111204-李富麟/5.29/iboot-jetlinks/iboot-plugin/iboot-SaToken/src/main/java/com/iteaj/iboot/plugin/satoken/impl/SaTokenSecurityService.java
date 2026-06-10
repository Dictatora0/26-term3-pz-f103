package com.iteaj.iboot.plugin.satoken.impl;

import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.iteaj.framework.Entity;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.captcha.Captcha;
import com.iteaj.framework.captcha.CaptchaService;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.JwtPrincipal;
import com.iteaj.framework.security.JwtRequestContext;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.security.PasswordCodec;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.security.SecurityService;
import com.iteaj.framework.security.SecurityToken;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SaTokenSecurityService implements SecurityService {

    private final CaptchaService captchaService;
    private final FrameworkProperties properties;
    private final AuthorizationService authorizationService;
    private final AuthenticationService authenticationService;

    public SaTokenSecurityService(CaptchaService captchaService,
                                  FrameworkProperties properties,
                                  AuthorizationService authorizationService,
                                  AuthenticationService authenticationService) {
        this.properties = properties;
        this.captchaService = captchaService;
        this.authorizationService = authorizationService;
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean isLogin() {
        return resolveJwtPrincipal().isPresent() || StpUtil.isLogin();
    }

    @Override
    public Optional<Entity> getUser() {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            return Optional.of(principal.get());
        }

        if (StpUtil.isLogin()) {
            TokenSign tokenSign = StpUtil.getSession().getTokenSignList().get(0);
            return Optional.ofNullable((Entity) tokenSign.getTag());
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> getRoles() {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            return Optional.of(principal.get().getRoles());
        }
        return getLoginId().map(authorizationService::getRoles);
    }

    @Override
    public boolean hasRole(String role) {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            return principal.get().getRoles().contains(role);
        }
        return StpUtil.hasRole(role);
    }

    @Override
    public boolean hasRole(Logical logical, String... roles) {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            List<String> userRoles = principal.get().getRoles();
            return logical == Logical.AND
                    ? userRoles.containsAll(Arrays.asList(roles))
                    : Arrays.stream(roles).anyMatch(userRoles::contains);
        }
        return logical == Logical.AND ? StpUtil.hasRoleAnd(roles) : StpUtil.hasRoleOr(roles);
    }

    @Override
    public boolean hasPermission(String permission) {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            return principal.get().getPermissions().contains(permission);
        }
        return StpUtil.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(Logical logical, String... permissions) {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            List<String> userPermissions = principal.get().getPermissions();
            return logical == Logical.AND
                    ? userPermissions.containsAll(Arrays.asList(permissions))
                    : Arrays.stream(permissions).anyMatch(userPermissions::contains);
        }
        return logical == Logical.AND ? StpUtil.hasPermissionAnd(permissions) : StpUtil.hasPermissionOr(permissions);
    }

    @Override
    public Collection<String> getSessionKeys() {
        return StpUtil.isLogin() ? StpUtil.getSession().keys() : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSessionAttr(String key) {
        return StpUtil.isLogin() ? Optional.ofNullable((T) StpUtil.getSession().get(key)) : Optional.empty();
    }

    @Override
    public SecurityService setSessionAttr(String key, Object value) {
        if (StpUtil.isLogin()) {
            StpUtil.getSession().set(key, value);
        }
        return this;
    }

    @Override
    public Object removeSessionAttr(String key) {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object value = StpUtil.getSession().get(key);
        StpUtil.getSession().delete(key);
        return value;
    }

    @Override
    public void logout(Serializable sessionId) {
        StpUtil.kickoutByTokenValue(sessionId.toString());
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) throws SecurityException {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public void login(SecurityToken token, HttpServletRequest request, HttpServletResponse response) throws SecurityException {
        validateCaptcha(token);

        AuthenticationUser byAccount = this.authenticationService.getByAccount(token.getAccount());
        if (byAccount == null) {
            throw SecurityException.unauthorized("账号不存在");
        }

        if (!byAccount.allowLogin()) {
            throw SecurityException.forbidden("账号已被禁用");
        }

        if (!PasswordCodec.matches(token.getPassword(), byAccount.getPassword())) {
            throw SecurityException.unauthorized("密码不匹配");
        }

        UserAgent agent = (UserAgent) request.getAttribute(CoreConst.WEB_USER_AGENT);
        SaLoginModel saLoginModel = new SaLoginModel()
                .setTokenSignTag(byAccount)
                .setIsLastingCookie(token.isRememberMe());

        FrameworkProperties.Session session = properties.getWeb().getSession();
        if (session.isWriteHeader()) {
            saLoginModel.setIsWriteHeader(true);
        }

        if (agent != null) {
            try {
                if (session.isAutomaticToken() && agent.getBrowser().getBrowserType() == BrowserType.APP) {
                    saLoginModel.setIsWriteHeader(true);
                }
                saLoginModel.setDevice(agent.getOperatingSystem().getDeviceType().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StpUtil.login(((Entity) byAccount).getId(), saLoginModel);
    }

    @Override
    public boolean isCurrentUser(Object sessionId) {
        Optional<JwtPrincipal> principal = resolveJwtPrincipal();
        if (principal.isPresent()) {
            return Objects.equals(String.valueOf(principal.get().getId()), String.valueOf(sessionId));
        }

        if (!StpUtil.isLogin()) {
            return false;
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return Objects.equals(tokenInfo.tokenValue, sessionId);
    }

    private void validateCaptcha(SecurityToken token) {
        FrameworkProperties.Security security = properties.getSecurity();
        if (security.isTestBypassCaptcha()) {
            if (!Objects.equals(security.getTestCaptcha(), token.getCaptcha())) {
                throw SecurityException.unauthorized("测试验证码不正确");
            }
            return;
        }

        String code = token.getCode();
        if (code == null) {
            throw SecurityException.unauthorized("验证码校验失败");
        }

        Captcha serviceCaptcha = captchaService.removeCaptcha(code);
        if (serviceCaptcha == null || serviceCaptcha.isExpire()) {
            throw SecurityException.unauthorized("验证码已失效");
        }

        if (!Objects.equals(serviceCaptcha.getCaptcha(), token.getCaptcha())) {
            throw SecurityException.unauthorized("验证码校验失败");
        }
    }

    private Optional<JwtPrincipal> resolveJwtPrincipal() {
        Object attribute = getRequest().getAttribute(JwtRequestContext.ATTR_PRINCIPAL);
        if (attribute instanceof JwtPrincipal) {
            return Optional.of((JwtPrincipal) attribute);
        }
        return Optional.empty();
    }
}
