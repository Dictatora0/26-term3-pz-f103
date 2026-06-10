package com.iteaj.framework.security;

import com.iteaj.framework.Entity;
import com.iteaj.framework.consts.CoreConst;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SecurityUtil {

    private static SecurityUtil instance;
    private static SecurityService securityService;

    protected SecurityUtil(SecurityService securityService) {
        SecurityUtil.securityService = securityService;
    }

    public synchronized static SecurityUtil getInstance(SecurityService securityService) {
        if (SecurityUtil.instance == null) {
            instance = new SecurityUtil(securityService);
        }
        return SecurityUtil.instance;
    }

    public static boolean isLogin() {
        return securityService.isLogin();
    }

    public static boolean isSuper() {
        return getLoginId()
                .map(item -> item.equals(1L))
                .orElse(false);
    }

    public static boolean isSuper(Serializable loginId) {
        return loginId.equals(1L);
    }

    public static boolean hasRole(Logical logical, String... roles) {
        if (isSuper()) {
            return true;
        }
        return securityService.hasRole(logical, roles);
    }

    public static boolean hasPermission(Logical logical, String... permissions) {
        if (isSuper()) {
            return true;
        }
        return securityService.hasPermission(logical, permissions);
    }

    public static boolean hasScope(Logical logical, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return true;
        }

        Optional<Entity> loginUser = getLoginUser();
        if (!loginUser.isPresent() || !(loginUser.get() instanceof JwtPrincipal)) {
            return false;
        }

        List<String> tokenScopes = ((JwtPrincipal) loginUser.get()).getScopes();
        if (logical == Logical.AND) {
            for (String scope : scopes) {
                if (!tokenScopes.contains(scope)) {
                    return false;
                }
            }
            return true;
        }

        for (String scope : scopes) {
            if (tokenScopes.contains(scope)) {
                return true;
            }
        }
        return false;
    }

    public static UserAgent getAgent() {
        return (UserAgent) getRequest().getAttribute(CoreConst.WEB_USER_AGENT);
    }

    public static HttpServletRequest getRequest() {
        return securityService.getRequest();
    }

    public static HttpServletResponse getResponse() {
        return securityService.getResponse();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getRequestAttr(String key) {
        return Optional.ofNullable((T) getRequest().getAttribute(key));
    }

    public static SecurityUtil setRequestAttr(String key, Object value) {
        getRequest().setAttribute(key, value);
        return SecurityUtil.instance;
    }

    public static Collection<String> getSessionKeys() {
        return securityService.getSessionKeys();
    }

    public static <T> Optional<T> getSessionAttr(String key) {
        return securityService.getSessionAttr(key);
    }

    public static SecurityService setSessionAttr(String key, Object value) {
        return securityService.setSessionAttr(key, value);
    }

    public static Object removeSessionAttr(String key) {
        return securityService.removeSessionAttr(key);
    }

    public static Optional<Entity> getLoginUser() {
        return securityService.getUser();
    }

    public static Optional<List<String>> getLoginRoles() {
        return securityService.getRoles();
    }

    public static Optional<Long> getLoginId() {
        return securityService.getLoginId().map(item -> {
            if (item instanceof Long) {
                return (Long) item;
            }
            return Long.valueOf(String.valueOf(item));
        });
    }

    public static void logout(Serializable sessionId) throws SecurityException {
        securityService.logout(sessionId);
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) throws SecurityException {
        securityService.logout(request, response);
    }

    public static void login(SecurityToken token, HttpServletRequest request, HttpServletResponse response) throws SecurityException {
        securityService.login(token, request, response);
    }

    public static boolean isCurrentUser(Object sessionId) {
        return securityService.isCurrentUser(sessionId);
    }
}
