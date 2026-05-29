package com.iteaj.iboot.plugin.message.dto;

import lombok.Data;

@Data
public class AcceptUser {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮件
     */
    private String email;
}
