package com.iteaj.iboot.plugin.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.Entity;
import lombok.Data;

@Data
@TableName("oauth2_app")
public class Oauth2App extends BaseEntity {

    /**
     * 应用id
     */
    private String clientId;

    /**
     * 应用密钥
     */
    private String clientSecret;

    /**
     * 应用状态(1.启用 2.禁用)
     */
    private Integer status;

    /**
     * 签约权限(多个逗号隔开)
     */
    private String contractScope;

    /**
     * 允许授权的url(多个逗号隔开)
     */
    private String allowUrl;

    /**
     * 应用名称
     */
    private String clientName;

    /**
     * 应用简称
     */
    private String abbreviate;

    /**
     * 备注
     */
    private String remark;

    /**
     * access_token保存时间(秒)
     */
    private Long accessTokenTimeout;

    /**
     * refresh_token保存时间(秒)
     */
    private Long refreshTokenTimeout;

    /**
     * client_token保存时间(秒)
     */
    private Long clientTokenTimeout;

    /**
     * past_token保存时间(秒)
     */
    private Long pastTokenTimeout;

    /**
     * 创建时间
     */
    private String createTime;
}
