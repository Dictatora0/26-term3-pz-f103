package com.iteaj.framework.autoconfigure;

import lombok.Data;

/**
 * create time: 2021/3/27
 *  认证相关配置信息
 * @author iteaj
 * @since 1.0
 */
@Data
public class AuthConfig {

    /**
     * 系统统一注销地址
     */
    private String logoutUri = "/auth/logout";

    /**
     * 原生app请求头的token名称
     */
    private String tokenName = "access_token";

    /**
     * 浏览器, 小程序等超时时间(秒) 默认30分钟
     */
    private long timeout = 30 * 60;

    /**
     * 原生app超时时间(秒) 默认30天
     */
    private long appTimeout = 30 * 24 * 60 * 60;
}
