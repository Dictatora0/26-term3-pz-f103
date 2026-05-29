package com.iteaj.iboot.plugin.message.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.framework.spi.message.SendModel;
import com.iteaj.framework.spi.message.sms.SmsMessageService;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.universal.SupplierConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 使用Sms4j框架实现的短信服务
 */
public abstract class Sms4jMessageService extends SmsMessageService {

    public Sms4jMessageService() {
        BaseProviderFactory providerFactory = buildBaseProviderFactory();
        if(providerFactory != null) {
            ProviderFactoryHolder.registerFactory(providerFactory);
        }
    }

    @Override
    public Optional<Object> send(SendModel model) {
        SmsResponse smsResponse = doSend(model, null);
        return Optional.of(smsResponse);
    }

    @Override
    public void sendAsync(SendModel model, Consumer callback) {
        doSend(model, callback);
    }

    /**
     * 发送短信
     * @param model
     * @return
     */
    protected SmsResponse doSend(SendModel model, Consumer callback) {
        if(CollectionUtil.isEmpty(model.getAccept())) {
            throw new ServiceException("接收人[accept]不能为空");
        }

        if(model.getTemplateId() == null) {
            if (Objects.isNull(model.getContent())) {
                throw new ServiceException("发送内容[content]不能为空");
            }
        }

        // 创建短信配置
        SmsBlend smsBlend = SmsFactory.getBySupplier(getChannelId());

        if(smsBlend == null) {
            throw new ServiceException("不支持消息类型["+getChannelId()+"]");
        }

        try {
            // 解析接收人
            SmsResponse smsResponse = null;
            List<String> accepts = model.getAccept();

            // 使用模板发送
            if(model.getTemplateId() != null) {
                if(callback == null) {
                    smsResponse = smsBlend.massTexting(accepts, model.getTemplateId()
                            , new LinkedHashMap<>(model.getTemplateVars()));
                } else {
                    for (int i = 0; i < accepts.size(); i++) {
                        smsBlend.sendMessageAsync(accepts.get(i), model.getTemplateId()
                                , model.getTemplateVars(), (response) -> {
                            if(!isSuccess(response)) {
                                logger.error("异步短信发送失败({}) {}", getChannelName(), response.getData());
                            }

                            callback.accept(response.getData());
                        });
                    }
                }
            } else {
                if(callback == null) {
                    smsResponse = smsBlend.massTexting(accepts, model.getContent().toString());
                } else {
                    for (int i = 0; i < accepts.size(); i++) {
                        smsBlend.sendMessageAsync(accepts.get(i), model.getContent().toString(), (response) -> {
                            if(!isSuccess(response)) {
                                logger.error("异步短信发送失败({}) {}", getChannelName(), response.getData());
                            }

                            callback.accept(response.getData());
                        });
                    }
                }
            }

            // 发送失败
            if(smsResponse != null && !isSuccess(smsResponse)) {
                logger.error("短信发送失败({}) {}", getChannelName(), smsResponse.getData());
            }

            return smsResponse;
        } catch (Exception e) {
            throw new ServiceException("短信发送异常["+getChannelId()+"]", e);
        }

    }

    protected abstract boolean isSuccess(SmsResponse response);

    @Override
    public MessageService build(JSONObject config) {
        BaseProviderFactory providerFactory = buildBaseProviderFactory();
        Object object = config.toJavaObject(providerFactory.getConfigClass());
        if(object instanceof BaseConfig) {
            ((BaseConfig) object).setConfigId(getConfigId());
        }

        SmsFactory.createSmsBlend((SupplierConfig) object);
        return this;
    }

    protected abstract BaseProviderFactory buildBaseProviderFactory();

    @Override
    public boolean remove() {
        return SmsFactory.unregister(getConfigId());
    }
}
