package com.iteaj.framework.spi.auth;

import cn.hutool.json.JSONUtil;
import com.iteaj.framework.result.HttpResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.web.WebUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;

/**
 * create time: 2021/3/24
 *  授权动作, 用于配置不同端的授权动作 比如：pc, 小程序, webapp
 *  使用同一套session, 支持session集群
 * @see #pathMatcher(String, HttpServletRequest) 通过uri匹配动作
 * @see WebAuthHandler
 * @author iteaj
 * @since 1.0
 */
public interface WebAuthAction<T> extends Ordered {

    /**
     * 默认session过期时间(30分钟)
     */
    long expireTime = 30 * 60;
    /**
     * ant路径匹配
     */
    PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    default int getOrder() {
        return 88;
    }

    /**
     * 每个授权动作的名称不能重复
     * @see WebAuthHandler#getAction(String) 通过名称获取一个动作
     * @return
     */
    default String getName() {
        return getClass().getName();
    }

    /**
     * session过期时间(秒)
     * @return
     */
    default long expireTime() {
        return expireTime;
    }

    /**
     * token名称 eg: access_token
     * 请求头携带的token
     * @return 不为null 则从请求header里面获取token作为sessionId
     */
    default String getTokenName() {
        return null;
    }

    /**
     * 是否是ajax请求
     * @param request
     * @return
     */
    default boolean isAjax(HttpServletRequest request) {
        String ajaxId = request.getHeader("x-requested-with");
        return "XMLHttpRequest".equalsIgnoreCase(ajaxId);
    }

    /**
     * 路径匹配
     * @param uri 不包含上下文路径
     * @param request
     * @return
     */
    default boolean pathMatcher(String uri, HttpServletRequest request) {
        for (String url : this.getUrlPattern()) {
            if(this.pathMatcher.match(url, uri)) {
                return true;
            }
        }
        return false;
    }

    default boolean isLoginRequest(HttpServletRequest request) {
        final String loginUrl = getLoginUrl();
        if(loginUrl != null) {
            String requestUri = WebUtils.getRequestUriContext(request);
            return loginUrl.equalsIgnoreCase(requestUri);
        }

        return false;
    }

    /**
     * 响应结果到客户端
     * @param result
     * @param response
     */
    default void writeJson(Result result, HttpServletResponse response) {
        response.setContentType("application/json;charset=utf-8");
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            String toJson = JSONUtil.toJsonStr(result);
            outputStream.write(toJson.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取登录地址
     * 登录的页面地址(GET)和提交表单登录地址(POST)必须相同
     * @return
     */
    default String getLoginUrl() {return null;}

    /**
     * 登录成功时跳转的url地址
     * @return
     */
    default String getSuccessUrl() { return null;}

    /**
     * 将符合此url模式的请求交由此action处理
     * @see org.springframework.util.PathMatcher#match(String, String)
     * 不包含上下文路径
     * @return
     */
    String[] getUrlPattern();

    /**
     * 调用授权之前的校验
     * @param request
     * @param response
     * @return true 需要授权 false 无需授权
     */
    boolean preAuthorize(HttpServletRequest request, HttpServletResponse response);

    /**
     * 用来处理未授权用户的请求
     * @param request 授权是的请求对象
     * @param response 授权是的响应对象
     * @return AuthContext 如果返回{@code null}直接返回
     */
    AuthContext getContext(HttpServletRequest request, HttpServletResponse response);

    /**
     * 继续授权
     * @param request
     * @param response
     * @return
     */
    boolean postAuthorize(HttpServletRequest request, HttpServletResponse response) throws SecurityException;

    /**
     * 将在提交登录请求时调用
     * @param request
     * @param response
     */
    AuthToken login(T t, HttpServletRequest request, HttpServletResponse response) throws SecurityException;

    /**
     * 注销
     * @param request
     * @param response
     */
    default void logout(HttpServletRequest request, HttpServletResponse response) {
        if(isAjax(request)) {
            writeJson(HttpResult.Success(getLoginUrl(), "注销成功"), response);
        } else {
            try {
                response.sendRedirect(getLoginUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 登录回调处理
     * @param cause 有无异常
     * @param request
     * @param response
     * @return 登录执行登录后是否继续拦截   false 拦截此请求  true 放行此请求
     */
    default boolean loginCall(Exception cause, HttpServletRequest request, HttpServletResponse response) {
        // 已经提交了响应到前端
        if(response.isCommitted()) {
            return false; // 拒绝放行
        }

        if(cause == null) { // 登录成功
            if(isAjax(request)) {
                writeJson(HttpResult.Success(getSuccessUrl(), "登录成功"), response);
            } else {
                try {
                    response.sendRedirect(getSuccessUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else { // 登录失败
            Throwable throwable = cause;
            if(cause instanceof UndeclaredThrowableException) {
                throwable = ((UndeclaredThrowableException) cause)
                        .getUndeclaredThrowable().getCause();
            }
            cause.printStackTrace();

            if(isAjax(request)) {
                if(throwable instanceof SecurityException) {
                    writeJson(HttpResult.Fail(throwable.getMessage()), response);
                } else {
                    writeJson(HttpResult.Fail("登录失败"), response);
                }
            } else {
                // 登录失败重定向到登录页面
                try {
                    response.sendRedirect(getLoginUrl());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
}
