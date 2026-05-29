package com.iteaj.framework.logger;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * create time: 2021/7/1
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AccessLogger {

    /**
     * 访问地址
     */
    private String url;

    /**
     * 功能简介
     */
    private String profile;

    /**
     * 访问ip
     */
    private String ip;

    /**
     * 访问时间(毫秒)
     */
    private long startTime;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 执行时间(毫秒)
     */
    private long millis;

    /**
     * 成功或者失败说明
     */
    private String errMsg;

    /**
     * 执行状态 true 成功 false 失败
     */
    private boolean status;

    private LoggerType type;

    private final static String ERR_MSG = "成功";

    public AccessLogger() {
        this.status = true;
        this.setErrMsg(ERR_MSG);
    }

    public AccessLogger(String url, LoggerType type, String profile) {
        this();
        this.url = url;
        this.type = type;
        this.profile = profile;
    }
}
