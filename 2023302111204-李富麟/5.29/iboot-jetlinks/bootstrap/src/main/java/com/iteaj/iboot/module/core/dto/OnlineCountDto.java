package com.iteaj.iboot.module.core.dto;

import lombok.Data;

@Data
public class OnlineCountDto {

    /**
     * 当前在线人数
     */
    private long currentOnline;

    /**
     * 当天访问人数
     */
    private long todayAccess;

    /**
     * 登录账号数
     */
    private long loginAccount;
}
