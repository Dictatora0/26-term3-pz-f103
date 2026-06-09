package com.iteaj.framework.security;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            checkPermission(method, handlerMethod.getBeanType());
            checkRole(method, handlerMethod.getBeanType());
            checkScope(method, handlerMethod.getBeanType());
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private void checkPermission(Method method, Class<?> beanType) {
        CheckPermission permission = method.getAnnotation(CheckPermission.class);
        if (permission == null) {
            permission = beanType.getAnnotation(CheckPermission.class);
        }

        if (permission != null && !SecurityUtil.hasPermission(permission.logical(), permission.value())) {
            throw new SecurityException("没有权限访问[" + join(permission.value()) + "]", 403);
        }
    }

    private void checkRole(Method method, Class<?> beanType) {
        CheckRole role = method.getAnnotation(CheckRole.class);
        if (role == null) {
            role = beanType.getAnnotation(CheckRole.class);
        }

        if (role != null && !SecurityUtil.hasRole(role.logical(), role.value())) {
            throw new SecurityException("没有角色访问[" + join(role.value()) + "]", 403);
        }
    }

    private void checkScope(Method method, Class<?> beanType) {
        CheckScope scope = method.getAnnotation(CheckScope.class);
        if (scope == null) {
            scope = beanType.getAnnotation(CheckScope.class);
        }

        if (scope != null && !SecurityUtil.hasScope(scope.logical(), scope.value())) {
            throw new SecurityException("访问令牌缺少授权范围[" + join(scope.value()) + "]", 403);
        }
    }

    private String join(String[] values) {
        return Arrays.stream(values).collect(Collectors.joining(","));
    }
}
