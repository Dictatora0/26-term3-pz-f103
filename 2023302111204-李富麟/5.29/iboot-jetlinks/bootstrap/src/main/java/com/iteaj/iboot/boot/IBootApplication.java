package com.iteaj.iboot.boot;

import com.iteaj.iboot.module.core.CoreAutoConfiguration;
import com.iteaj.iboot.module.iot.IotAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 请不用指定参数{@link SpringBootApplication#scanBasePackages()} <hr>
 *     将在各自的模块启用要扫描的包 {@link ComponentScan#basePackages()}
 * @see CoreAutoConfiguration
 *
 * 以下导入的模块可以通过两种方式来启用或者停用 <hr>
 *     1. 直接通过配置文件 {@see application.properties} spring.profiles.include=iot,qrtz
 *     2. 直接注释掉
 */
@ImportAutoConfiguration(value = {
        IotAutoConfiguration.class, // 物联网模块

        // 以下是IBoot的核心配置(必须启用)
        CoreAutoConfiguration.class, // 系统管理模块
})
@SpringBootApplication
public class IBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(IBootApplication.class, args);
    }

}
