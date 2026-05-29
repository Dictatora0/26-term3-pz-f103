package com.iteaj.iboot.plugin.message.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.message.MessageConfig;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.framework.spi.message.SendModel;
import com.iteaj.framework.spi.message.email.EmailMessageService;
import org.dromara.email.api.MailClient;
import org.dromara.email.comm.config.MailSmtpConfig;
import org.dromara.email.comm.entity.MailMessage;
import org.dromara.email.core.factory.MailFactory;
import org.dromara.sms4j.provider.factory.BeanFactory;

import java.util.Optional;
import java.util.function.Consumer;

public class Sms4jEmailMessageService extends EmailMessageService {

    private MailClient client;

    @Override
    public Optional<Object> send(SendModel model) {
        if(CollectionUtil.isEmpty(model.getAccept())) {
            throw new ServiceException("未指定接收人");
        }

        MailMessage mailMessage = MailMessage.Builder()
                .mailAddress(model.getAccept())
                .title(model.getTitle())
                .htmlContent(model.getContent().toString())
                .build();
        client.send(mailMessage);
        return Optional.of(client);
    }

    @Override
    public void sendAsync( SendModel model, Consumer callback) {
        BeanFactory.getExecutor().execute(() -> {
            try {
                send(model);
                callback.accept(this.client);
            } catch (Exception e) {
                callback.accept(e);
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean remove() {
        MailFactory.put(getType() + ":" + getChannelId(), null);
        return true;
    }

    @Override
    public MessageService build(JSONObject config) {
        MailSmtpConfig build = MailSmtpConfig.builder()
                .fromAddress(config.getString("fromAddress"))
                .isAuth(config.getString("isAuth"))
                .isSSL(config.getString("isSSL"))
                .smtpServer(config.getString("smtpServer"))
                .username(config.getString("username"))
                .password(config.getString("password"))
                .port(config.getString("port"))
                .build();

        String key = getConfigId();
        MailFactory.put(key, build);
        try {
            this.client = MailFactory.createMailClient(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
