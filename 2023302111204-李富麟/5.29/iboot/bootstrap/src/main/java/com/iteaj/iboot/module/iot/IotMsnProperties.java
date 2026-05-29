package com.iteaj.iboot.module.iot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "msn.iot")
public class IotMsnProperties {

    /**
     * 采集调度配置
     */
    private CollectTask task;

    /**
     * 采集任务配置
     */
    @Getter
    @Setter
    public static class CollectTask {
        /**
         * 核心线程池大小
         */
        private Integer poolSize;

        /**
         * 线程池前缀
         */
        private String threadNamePrefix = "ICT-";
    }
}
