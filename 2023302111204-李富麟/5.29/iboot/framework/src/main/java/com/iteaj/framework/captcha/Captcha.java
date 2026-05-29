package com.iteaj.framework.captcha;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
@TableName("sys_captcha")
public class Captcha {

    private Long id;

    private String code;

    private String captcha;

    private Long expireTime;

    private Date createTime;

    public Captcha() { }

    public Captcha(String code, String captcha, long expire) {
        this.code = code;
        this.captcha = captcha;
        this.expireTime = expire;
        this.createTime = new Date();
    }

    public boolean isExpire() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - createTime.getTime()) > this.getExpireTime();
    }
}
