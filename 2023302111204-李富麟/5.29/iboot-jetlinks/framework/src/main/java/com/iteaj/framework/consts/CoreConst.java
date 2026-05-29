package com.iteaj.framework.consts;

/**
 * create time: 2021/3/24
 *
 * @author iteaj
 * @since 1.0
 */
public interface CoreConst {

    String FRAMEWORK_FILTER_NAME = "framework";

    String CAPTCHA_CODE = "code";

    String WEB_USER_AGENT = "WEB_USER_AGENT";

    String WEB_LOGIN_USER = "WEB_LOGIN_USER";

    String SESSION_AUTH_CONTEXT = "SESSION_AUTH_CONTEXT";

    String WEB_SESSION_CACHE = "WEB_SESSION_CACHE";

    /**
     * 执行异常状态状态
     */
    String EXEC_EXCEPTION_STATUS = "EXEC_EXCEPTION_STATUS";

    /**
     * 请求
     */
    String HTTP_SERVLET_REQUEST = "HTTP_SERVLET_REQUEST";

    /**
     * 是否是OAuth2重定向请求
     */
    String OAUTH2_REDIRECT_REQUEST = "OAUTH2_REDIRECT_REQUEST";

    /**
     * 处理授权动作
     */
    String HANDLE_AUTH_ACTION = "HANDLE_AUTH_ACTION";

    /**
     * 在线用户标识
     */
    String ONLINE_ACCOUNT_ID = "ONLINE_ACCOUNT_ID";

    /**
     * session缓存前缀
     */
    String SESSION_KEY_PREFIX = "WEB:SESSION:";
    /**
     * 权限缓存前缀
     */
    String AUTHORIZATION_KEY_PREFIX = "IFAOCF:";
    /**
     * 认证缓存前缀
     */
    String AUTHENTICATION_KEY_PREFIX = "IFAECF:";
}
