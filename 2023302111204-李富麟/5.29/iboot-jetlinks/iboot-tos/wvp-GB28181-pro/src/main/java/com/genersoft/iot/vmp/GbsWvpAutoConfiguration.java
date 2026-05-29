package com.genersoft.iot.vmp;

import com.genersoft.iot.vmp.conf.MediaConfig;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;

@ComponentScan({"com.genersoft.iot.vmp"})
@ServletComponentScan("com.genersoft.iot.vmp.conf")
@MapperScan("com.genersoft.iot.vmp.storager.dao")
@EnableConfigurationProperties(MediaConfig.class)
public class GbsWvpAutoConfiguration {

    /**
     * @return
     */
    @Bean
    @Order(value = 18)
    public OrderFilterChainDefinition gbsFilterChainDefinition() {
        return new OrderFilterChainDefinition().addAnon("/gbs/zlm/hook/**");
    }
}
