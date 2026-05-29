package com.iteaj.iboot.plugin.message.service;

import org.dromara.sms4j.aliyun.config.AlibabaFactory;
import org.dromara.sms4j.cloopen.config.CloopenFactory;
import org.dromara.sms4j.ctyun.config.CtyunFactory;
import org.dromara.sms4j.emay.config.EmayFactory;
import org.dromara.sms4j.huawei.config.HuaweiFactory;
import org.dromara.sms4j.jdcloud.config.JdCloudFactory;
import org.dromara.sms4j.netease.config.NeteaseFactory;
import org.dromara.sms4j.tencent.config.TencentFactory;
import org.dromara.sms4j.unisms.config.UniFactory;
import org.dromara.sms4j.yunpian.config.YunPianFactory;

public enum SmsChannelType {

    /** 阿里云*/
    ALIBABA("阿里云短信"),
    /** 华为云*/
    HUAWEI("华为云短信"),
    /** 云片*/
    YUNPIAN("云片短信"),
    /** 腾讯云*/
    TENCENT("腾讯云短信"),
    /** 合一短信*/
    UNI_SMS("合一短信"),
    /** 京东云 */
    JD_CLOUD("京东云短信"),
    /** 容联云 */
    CLOOPEN("容联云短信"),
    /** 亿美软通*/
    EMAY("亿美软通"),
    /** 天翼云 */
    CTYUN("天翼云短信"),
    /** 网易云信 */
    NETEASE("网易云短信")
    ;

    public String name;

    SmsChannelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
