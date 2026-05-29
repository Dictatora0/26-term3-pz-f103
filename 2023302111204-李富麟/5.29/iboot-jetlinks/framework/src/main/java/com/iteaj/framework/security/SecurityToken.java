package com.iteaj.framework.security;

import lombok.Data;

@Data
public class SecurityToken {

    /**
     * 校验码token
     */
    private String code;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户名
     * @see #account
     */
    @Deprecated
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    private boolean rememberMe = false;

    public void setUsername(String username) {
        this.account = username;
        this.username = username;
    }
}
