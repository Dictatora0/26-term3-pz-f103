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
        if(handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();

            // 开始做权限校验
            CheckPermission permission = method.getAnnotation(CheckPermission.class);
            if(permission != null) { // 先检查方法上的权限注解
                if(!SecurityUtil.hasPermission(permission.logical(), permission.value())) {
                    throw new SecurityException("没有权限访问["+ Arrays.stream(permission.value()).collect(Collectors.joining())+"]");
                }
            } else { // 如果方法上没有使用类级别的注解
                Class<?> beanType = ((HandlerMethod) handler).getBeanType();
                CheckPermission annotation = beanType.getAnnotation(CheckPermission.class);
                if(annotation != null) {
                    if(!SecurityUtil.hasPermission(annotation.logical(), annotation.value())) {
                        throw new SecurityException("没有权限访问["+Arrays.stream(annotation.value()).collect(Collectors.joining())+"]");
                    }
                }
            }

            // 做角色校验
            CheckRole checkRole = method.getAnnotation(CheckRole.class);
            if(checkRole != null) {// 先检查方法上的角色注解
                if(!SecurityUtil.hasRole(checkRole.logical(), checkRole.value())) {
                    throw new SecurityException("没有角色访问["+ Arrays.stream(checkRole.value()).collect(Collectors.joining())+"]");
                }
            } else {// 如果方法上没有使用类级别的角色注解
                Class<?> beanType = ((HandlerMethod) handler).getBeanType();
                CheckRole annotation = beanType.getAnnotation(CheckRole.class);
                if(annotation != null) {
                    if(!SecurityUtil.hasPermission(annotation.logical(), annotation.value())) {
                        throw new SecurityException("没有权限访问["+Arrays.stream(annotation.value()).collect(Collectors.joining())+"]");
                    }
                }
            }
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
