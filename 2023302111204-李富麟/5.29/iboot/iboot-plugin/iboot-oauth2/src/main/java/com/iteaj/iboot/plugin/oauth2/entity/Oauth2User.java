package com.iteaj.iboot.plugin.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.Entity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("oauth2_user")
public class Oauth2User extends BaseEntity {

    private Long id;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 登录用户id
     */
    private String loginId;

    /**
     * 应用id
     */
    private String clientId;

    /**
     * 创建时间
     */
    private Date createTime;
}
