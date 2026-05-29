package com.iteaj.iboot.plugin.message;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.framework.ProfilesInclude;
import com.iteaj.framework.spi.admin.Module;
import com.iteaj.framework.spi.message.MessageManager;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.iboot.plugin.message.service.AlibabaSmsService;
import com.iteaj.iboot.plugin.message.service.IMessageSourceService;
import com.iteaj.iboot.plugin.message.service.IMessageTemplateService;
import com.iteaj.iboot.plugin.message.service.Sms4jEmailMessageService;
import org.dromara.sms4j.provider.config.SmsConfig;
import org.dromara.sms4j.provider.factory.BeanFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ProfilesInclude("msg")
@ComponentScan({"com.iteaj.iboot.plugin.message"})
@MapperScan({"com.iteaj.iboot.plugin.message.mapper"})
@EnableConfigurationProperties(MsgProperties.class)
public class MessageAutoConfiguration implements InitializingBean {

    private final MsgProperties properties;

    public MessageAutoConfiguration(MsgProperties properties) {
        this.properties = properties;
    }

    /**
     * 消息模块
     * @return
     */
    @Bean
    public Module msgModule() {
        return Module.module("msg", "消息模块(短信、邮件、OA...)", 66666);
    }

    @Bean
    public MessageManager messageManager(@Autowired(required = false) List<MessageService> services
            , IMessageSourceService service, IMessageTemplateService templateService) {
        MessageManager messageManager = new MessageManager(services);
        if(CollectionUtil.isNotEmpty(services)) {
            Map<String, MessageService> serviceMap = services.stream().collect(Collectors.toMap(MessageService::getType, item -> item));
            service.list().forEach(source -> {
                MessageService messageService = serviceMap.get(source.getType());
                if(messageService != null) {
                    messageService.build(source.getConfig());
                    messageManager.registerOrUpdateDefault(messageService);
                }
            });
        }

        return messageManager;
    }

    @Bean
    public AlibabaSmsService alibabaSmsService() {
        return new AlibabaSmsService();
    }

    @Bean
    public Sms4jEmailMessageService sms4jEmailMessageService() {
        return new Sms4jEmailMessageService();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SmsConfig smsConfig = BeanFactory.getSmsConfig();
        smsConfig.setMaxPoolSize(properties.getMaxPoolSize());
        smsConfig.setCorePoolSize(properties.getCorePoolSize());
        smsConfig.setQueueCapacity(properties.getQueueCapacity());
        smsConfig.setShutdownStrategy(properties.getShutdownStrategy());
    }
}
