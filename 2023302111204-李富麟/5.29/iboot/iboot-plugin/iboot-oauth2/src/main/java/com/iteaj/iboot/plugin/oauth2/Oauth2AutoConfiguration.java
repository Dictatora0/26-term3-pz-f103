package com.iteaj.iboot.plugin.oauth2;

import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import com.iteaj.framework.plugin.Plugin;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import com.iteaj.iboot.plugin.oauth2.sa.SaOAuth2TemplateImpl;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;

@MapperScan("com.iteaj.iboot.plugin.oauth2.mapper")
@ComponentScan("com.iteaj.iboot.plugin.oauth2")
public class Oauth2AutoConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered, Plugin {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        event.getEnvironment().addActiveProfile("oauth2");
    }

    @Bean
    public OrderFilterChainDefinition oauth2FilterChainDefinition() {
        return new OrderFilterChainDefinition().addAnon("/oauth2/**");
    }

    @Bean
    public SaOAuth2Template saOAuth2TemplateImpl(Oauth2AppService oauth2AppService, Oauth2UserService oauth2UserService) {
        return new SaOAuth2TemplateImpl(oauth2AppService, oauth2UserService);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }

    @Override
    public String getPluginName() {
        return "Oauth2";
    }

    @Override
    public String getPluginDesc() {
        return "基于SaToken实现的Oauth2 Server授权框架";
    }
}
