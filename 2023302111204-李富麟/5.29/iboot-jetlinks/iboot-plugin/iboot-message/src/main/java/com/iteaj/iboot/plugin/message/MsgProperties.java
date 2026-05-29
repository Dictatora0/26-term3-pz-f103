package com.iteaj.iboot.plugin.message;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;

@Data
@ConfigurationProperties(prefix = "iboot.plugin.sms")
public class MsgProperties {
    /**
     * 是否开启短信限制
     */
    private Boolean restricted = false;

    /**
     * 单账号每日最大发送量
     */
//    private Integer accountMax;

    /**
     * 单账号每分钟最大发送
     */
//    private Integer minuteMax;

    /**
     * 核心线程池大小
     */
    private Integer corePoolSize = 2;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize = 10;

    /**
     * 队列容量
     */
    private Integer queueCapacity = 500;

    /**
     * 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
     */
    private Boolean shutdownStrategy = true;

    /**
     * 黑名单配置 此配置不支持yaml读取
     */
//    private ArrayList<String> blackList = new ArrayList<>();
}
