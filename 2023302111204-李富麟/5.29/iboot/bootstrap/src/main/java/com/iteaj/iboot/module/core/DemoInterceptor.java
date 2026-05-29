package com.iteaj.iboot.module.core;

import cn.hutool.json.JSONUtil;
import com.iteaj.framework.result.Result;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 演示拦截器
 */
@Component
@Profile("test")
public class DemoInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private final CoreProperties coreProperties;

    public DemoInterceptor(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestURI = request.getRequestURI();

        // 注销不拦截
        if(requestURI.contains("logout")) {
            return true;
        }

        final String method = request.getMethod();
        // 其他post接口全部拦截
        if("POST".equalsIgnoreCase(method)) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSONUtil.toJsonStr(Result.fail("演示环境不允许操作")));
            return false;
        }

        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).excludePathPatterns(coreProperties.getLoginUrl());
    }
}
