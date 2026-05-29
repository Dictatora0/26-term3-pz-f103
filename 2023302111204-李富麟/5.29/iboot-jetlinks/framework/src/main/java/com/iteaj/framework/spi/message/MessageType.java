package com.iteaj.framework.spi.message;

public enum MessageType {
    Sms("短信"),
    Email("邮件")
    ;

    public String desc;

    MessageType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
