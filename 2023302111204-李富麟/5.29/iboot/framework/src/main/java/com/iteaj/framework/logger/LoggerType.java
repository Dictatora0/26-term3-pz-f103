package com.iteaj.framework.logger;

/**
 * create time: 2020/4/21
 *  日志类型
 * @author iteaj
 * @since 1.0
 */
public enum LoggerType {
    Func("功能日志"), Login("登录日志"), Logout("注销日志");

    public String desc;

    LoggerType(String desc) {
        this.desc = desc;
    }
}
