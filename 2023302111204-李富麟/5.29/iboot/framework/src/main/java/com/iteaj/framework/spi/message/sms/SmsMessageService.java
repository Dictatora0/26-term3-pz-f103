package com.iteaj.framework.spi.message.sms;

import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.framework.spi.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 短信服务
 */
public abstract class SmsMessageService implements MessageService {

    private List<ParamMeta> params = new ArrayList<>();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public SmsMessageService() {
        this.buildConfigParams(this.params);
    }

    protected void buildConfigParams(List<ParamMeta> params) {
        params.add(ParamMeta.buildRequired("accessKeyId", "accessKeyId"));
        params.add(ParamMeta.buildRequired("accessKeySecret", "accessKeySecret"));
        params.add(ParamMeta.buildRequired("signature", "signature"));
    }


    @Override
    public List<ParamMeta> getConfigParams() {
        return params;
    }

    @Override
    public String getType() {
        return MessageType.Sms.name();
    }

    @Override
    public String getName() {
        return MessageType.Sms.getDesc();
    }

    @Override
    public abstract String getChannelId();

    @Override
    public abstract String getChannelName();
}
