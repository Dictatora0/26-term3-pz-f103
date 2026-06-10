package com.iteaj.iboot.plugin.satoken;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoRedisJackson;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.iteaj.framework.ProfilesInclude;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.captcha.CaptchaService;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.result.HttpResult;
import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.JwtAccessTokenService;
import com.iteaj.framework.security.JwtPrincipal;
import com.iteaj.framework.security.JwtRequestContext;
import com.iteaj.framework.security.JwtRevocationService;
import com.iteaj.framework.security.JwtTokenClaims;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.security.SecurityInterceptor;
import com.iteaj.framework.security.SecurityService;
import com.iteaj.iboot.plugin.satoken.impl.SaTokenSecurityService;
import com.iteaj.iboot.plugin.satoken.impl.StpInterfaceImpl;
import com.iteaj.iboot.plugin.satoken.listener.SaTokenOnlineListener;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@ProfilesInclude("satoken")
public class SaTokenAutoConfiguration implements WebMvcConfigurer {

    private final List<OrderFilterChainDefinition> definitions;

    public SaTokenAutoConfiguration(List<OrderFilterChainDefinition> definitions) {
        this.definitions = definitions;
    }

    @Bean
    public SaTokenOnlineListener saTokenOnlineListener() {
        return new SaTokenOnlineListener();
    }

    @Bean
    public StpInterface stpInterfaceImpl(AuthorizationService authorizationService) {
        return new StpInterfaceImpl(authorizationService);
    }

    @Bean
    public SecurityService securityService(CaptchaService captchaService,
                                           FrameworkProperties properties,
                                           AuthorizationService authorizationService,
                                           AuthenticationService authenticationService) {
        return new SaTokenSecurityService(captchaService, properties, authorizationService, authenticationService);
    }

    @Bean
    public JwtRevocationService jwtRevocationService(
            ObjectProvider<org.springframework.data.redis.core.RedisTemplate> redisTemplateProvider) {
        return new JwtRevocationService(redisTemplateProvider);
    }

    @Bean
    public JwtAccessTokenService jwtAccessTokenService(FrameworkProperties properties,
                                                       JwtRevocationService jwtRevocationService) {
        return new JwtAccessTokenService(properties, jwtRevocationService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new SecurityInterceptor()).addPathPatterns("/**");
        definitions.forEach(item -> item.getFilterChainMap().forEach((key, value) -> {
            if ("anon".equals(value)) {
                registration.excludePathPatterns(key);
            }
        }));
    }

    @Bean
    @com.iteaj.framework.spring.condition.ConditionalOnRedis
    @com.iteaj.framework.spring.condition.ConditionalOnCluster
    public SaTokenDao saTokenDao() {
        return new SaTokenDaoRedisJackson();
    }

    @Bean
    public SaServletFilter getSaServletFilter(List<OrderFilterChainDefinition> definitions,
                                              FrameworkProperties properties,
                                              JwtAccessTokenService jwtAccessTokenService) {
        SaServletFilter servletFilter = new SaServletFilter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest servletRequest = (HttpServletRequest) request;
                UserAgent userAgent = UserAgent.parseUserAgentString(servletRequest.getHeader("user-agent"));
                servletRequest.setAttribute(CoreConst.WEB_USER_AGENT, userAgent);
                try {
                    attachJwtPrincipal(servletRequest, properties, jwtAccessTokenService);
                } catch (SecurityException e) {
                    HttpServletResponse servletResponse = (HttpServletResponse) response;
                    servletResponse.setStatus(e.getStatusCode() > 0 ? e.getStatusCode() : HttpServletResponse.SC_UNAUTHORIZED);
                    servletResponse.setContentType("application/json;charset=UTF-8");
                    servletResponse.getWriter().write(JSONUtil.toJsonStr(
                            HttpResult.StatusCode(null, e.getMessage(), servletResponse.getStatus())
                    ));
                    return;
                }
                super.doFilter(request, response, chain);
            }
        };

        definitions.forEach(item -> item.getFilterChainMap().forEach((key, value) -> {
            if ("anon".equals(value)) {
                servletFilter.addExclude(key);
            } else {
                servletFilter.addInclude(key);
            }
        }));

        return servletFilter
                .setAuth(obj -> {
                    HttpServletRequest request = (HttpServletRequest) SaHolder.getRequest().getSource();
                    if (Boolean.TRUE.equals(request.getAttribute(JwtRequestContext.ATTR_AUTHENTICATED))) {
                        return;
                    }
                    StpUtil.checkLogin();
                })
                .setError(e -> {
                    SaResponse response = SaHolder.getResponse();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("Content-Type", "application/json;charset=UTF-8");
                    return JSONUtil.toJsonStr(HttpResult.StatusCode(null, "unauthorized", HttpServletResponse.SC_UNAUTHORIZED));
                });
    }

    private void attachJwtPrincipal(HttpServletRequest request,
                                    FrameworkProperties properties,
                                    JwtAccessTokenService jwtAccessTokenService) {
        FrameworkProperties.Security security = properties.getSecurity();
        if (!security.isEnableJwt()) {
            return;
        }

        String token = resolveBearerToken(request, security);
        if (token == null || token.isEmpty()) {
            return;
        }

        JwtTokenClaims claims;
        try {
            claims = jwtAccessTokenService.resolveClaims(token);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw SecurityException.unauthorized("访问令牌无效");
        }

        JwtPrincipal principal = new JwtPrincipal();
        principal.setId(claims.getUserId());
        principal.setAccount(claims.getAccount());
        principal.setName(claims.getDisplayName());
        principal.setClientId(claims.getClientId());
        principal.setJti(claims.getJti());
        principal.setIssuer(claims.getIssuer());
        principal.setRoles(claims.getRoles());
        principal.setPermissions(claims.getPermissions());
        principal.setScopes(claims.getScopes());

        request.setAttribute(JwtRequestContext.ATTR_PRINCIPAL, principal);
        request.setAttribute(JwtRequestContext.ATTR_AUTHENTICATED, true);
    }

    private String resolveBearerToken(HttpServletRequest request, FrameworkProperties.Security security) {
        String authHeader = request.getHeader(security.getAuthorizationHeader());
        if (authHeader != null && authHeader.startsWith(security.getBearerPrefix())) {
            return authHeader.substring(security.getBearerPrefix().length()).trim();
        }

        if (security.isAllowLegacyHeader()) {
            String legacyToken = request.getHeader("access_token");
            if (legacyToken != null && !legacyToken.trim().isEmpty()) {
                return legacyToken.trim();
            }
        }

        return null;
    }
}
