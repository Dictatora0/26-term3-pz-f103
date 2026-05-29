package com.iteaj.framework.spi.app;

import com.iteaj.framework.spi.auth.AccountAuthAction;
import com.iteaj.framework.web.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * create time: 2021/4/4
 *  原生app授权认证动作
 * @author iteaj
 * @since 1.0
 */
public abstract class NativeAuthAction extends AccountAuthAction {

    public NativeAuthAction(String... urlPattern) {
        super(urlPattern);
    }

    @Override
    public boolean pathMatcher(String uri, HttpServletRequest request) {
        return WebUtils.isApp(request) && super.pathMatcher(uri, request);
    }

    @Override
    public boolean preAuthorize(HttpServletRequest request, HttpServletResponse response) {
        return isLoginRequest(request);
    }

    /**
     * native app 的请求都当成ajax请求处理
     * @param request
     * @return
     */
    @Override
    public boolean isAjax(HttpServletRequest request) {
        return true;
    }

}
