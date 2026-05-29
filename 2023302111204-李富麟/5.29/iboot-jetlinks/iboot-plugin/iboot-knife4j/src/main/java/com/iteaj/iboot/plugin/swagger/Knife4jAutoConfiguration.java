package com.iteaj.iboot.plugin.swagger;

import com.iteaj.framework.ProfilesInclude;
import com.iteaj.framework.plugin.Plugin;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;

@ProfilesInclude("knife4j")
public class Knife4jAutoConfiguration implements Plugin {

    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public OrderFilterChainDefinition swaggerFilterChainDefinition() {
        return new OrderFilterChainDefinition().addAnon("/swagger/**", "/swagger-resources/**", "/swagger-instance/**"
                , "/doc.html", "/webjars/**", "/openapi/**", "/v3/api-docs/**");
    }

    @Override
    public String getPluginName() {
        return "Knife4j";
    }

    @Override
    public String getPluginDesc() {
        return "使用Smart-doc和knife4j框架实现的文档";
    }

    @Override
    public void banner() {
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        System.out.println(String.format("------------------------------------------------- 启用knife4j api文档插件 -----------------------------------------------\r\n" +
                        "                              knife4j访问地址：http://127.0.0.1:%s%s%s)                                           \r\n" +
                        "                       smart-doc访问地址：http://127.0.0.1:%s%s%s)                              \r\n" +
                        "-----------------------------------------------------------------------------------------------------------------------"
                , port + "", contextPath, "/doc.html", port + "", contextPath, "/static/doc/index.html"));
    }

}
