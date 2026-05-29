package com.iteaj.iboot.plugin.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.iteaj.framework.Entity;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.security.*;
import com.iteaj.framework.security.SecurityException;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ShiroSecurityService implements SecurityService {

    private final FrameworkProperties properties;
    private final AuthorizationService authorizationService;

    public ShiroSecurityService(FrameworkProperties properties
            , AuthorizationService authorizationService) {
        this.properties = properties;
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean isLogin() {
        return SecurityUtils.getSubject().getPrincipal() != null;
    }

    public Optional<Entity> getUser() {
        return Optional.ofNullable((Entity) SecurityUtils.getSubject().getPrincipal());
    }

    @Override
    public Optional<List<String>> getRoles() {
        return getLoginId().map(item -> authorizationService.getRoles(item));
    }

    @Override
    public boolean hasRole(String role) {
        return SecurityUtils.getSubject().hasRole(role);
    }

    @Override
    public boolean hasRole(Logical logical, String... roles) {
        if(logical == Logical.AND) {
            return SecurityUtils.getSubject().hasAllRoles(Arrays.asList(roles));
        } else {
            for(boolean item : SecurityUtils.getSubject().hasRoles(Arrays.asList(roles))) {
                if(item) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return SecurityUtils.getSubject().isPermitted(permission);
    }

    @Override
    public boolean hasPermission(Logical logical, String... permissions) {
        if(logical == Logical.AND) {
            return SecurityUtils.getSubject().isPermittedAll(permissions);
        } else {
            boolean[] permitted = SecurityUtils.getSubject().isPermitted(permissions);
            for(boolean item : permitted) {
                if(item) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public Collection<String> getSessionKeys() {
        Collection<Object> keys = SecurityUtils.getSubject().getSession().getAttributeKeys();
        return keys != null ? keys.stream().map(key -> (String) key).collect(Collectors.toList()) : Collections.EMPTY_LIST;
    }

    @Override
    public <T> Optional<T> getSessionAttr(String key) {
        Session session = SecurityUtils.getSubject().getSession();
        return Optional.ofNullable((T) session.getAttribute(key));
    }

    @Override
    public SecurityService setSessionAttr(String key, Object value) {
        SecurityUtils.getSubject().getSession().setAttribute(key, value);
        return this;
    }

    @Override
    public Object removeSessionAttr(String key) {
        Session session = SecurityUtils.getSubject().getSession();
        return session.removeAttribute(key);
    }

    @Override
    public void logout(Serializable sessionId) {
        DefaultSessionKey sessionKey = new DefaultSessionKey(sessionId);
        Session session = SecurityUtils.getSecurityManager().getSession(sessionKey);
        if(session != null) {
            session.stop();
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            SecurityUtils.getSubject().logout();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void login(SecurityToken token, HttpServletRequest request, HttpServletResponse response) {
        ShiroSecurityToken securityToken = new ShiroSecurityToken();
        BeanUtil.copyProperties(token, securityToken);

        try {
            SecurityUtils.getSubject().login(securityToken);

            FrameworkProperties.Web web = properties.getWeb();
            FrameworkProperties.Session sessionConfig = web.getSession();

            // 将token写入到header
            if(sessionConfig.isWriteHeader()) {
                String tokenName = sessionConfig.getTokenName();
                Session session = SecurityUtils.getSubject().getSession();
                response.setHeader(tokenName, session.getId().toString());
            } else {
                UserAgent agent = (UserAgent) request.getAttribute(CoreConst.WEB_USER_AGENT);
                // 自动选择token的方式下 app请求需要将token写入到header
                if(agent != null && sessionConfig.isAutomaticToken()) {
                    // 如果是app
                    try {
                        if(agent.getBrowser().getBrowserType() == BrowserType.APP) {
                            String tokenName = sessionConfig.getTokenName();
                            Session session = SecurityUtils.getSubject().getSession();
                            response.setHeader(tokenName, session.getId().toString());
                        }
                    } catch (Exception e) { // 如果异常不做任何处理
                        e.printStackTrace();
                    }
                }
            }
        } catch (AuthenticationException e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isCurrentUser(Object sessionId) {
        Session session = SecurityUtils.getSubject().getSession();
        return Objects.equals(session.getId(), sessionId);
    }
}
