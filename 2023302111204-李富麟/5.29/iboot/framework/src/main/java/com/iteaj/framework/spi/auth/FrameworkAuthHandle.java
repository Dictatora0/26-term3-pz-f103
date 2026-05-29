package com.iteaj.framework.spi.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * create time: 2021/4/1
 *
 * @author iteaj
 * @since 1.0
 */
public interface FrameworkAuthHandle<T> {
    /**
     * 将在app端提交登录请求时调用
     * @param request
     * @param response
     */
    AuthToken login(T t, HttpServletRequest request, HttpServletResponse response);

    /**
     * 注销动作
     * @param request
     * @param response
     */
    default void logout(HttpServletRequest request, HttpServletResponse response){ }
}
