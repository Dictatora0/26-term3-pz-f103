package com.iteaj.iboot.module.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * create time: 2021/4/3
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "msn.core")
public class CoreProperties {
    /**
     * 登录页的url(GET)和提交的url(POST)必须相同
     */
    private String loginUrl;

    /** 认证成功后要跳转的Url*/
    private String successUrl;

    /** 认证失败后要跳转的Url */
    private String unauthorizedUrl;


    /** 验证码类型, 默认数字相加 e.g. 3+3*/
    private String captchaType = "math";
}
