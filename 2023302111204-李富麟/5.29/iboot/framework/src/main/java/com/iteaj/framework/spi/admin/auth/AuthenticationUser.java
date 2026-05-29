package com.iteaj.framework.spi.admin.auth;

public interface AuthenticationUser {

    /**
     * 是否允许登录
     * @return
     */
    boolean allowLogin();

    /**
     * 获取账号信息
     * @return
     */
    String getAccount();

    /**
     * 获取密码
     * @return
     */
    String getPassword();
}
