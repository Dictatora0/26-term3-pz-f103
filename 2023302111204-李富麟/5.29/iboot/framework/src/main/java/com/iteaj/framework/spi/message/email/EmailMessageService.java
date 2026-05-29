package com.iteaj.framework.spi.message.email;

import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.framework.spi.message.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务
 */
public abstract class EmailMessageService implements MessageService {

    private List<ParamMeta> params = new ArrayList<>();

    public EmailMessageService() {
        this.buildConfigParams();
    }

    @Override
    public String getType() {
        return MessageType.Email.name();
    }

    @Override
    public String getName() {
        return MessageType.Email.getDesc();
    }

    protected void buildConfigParams() {
        params.add(ParamMeta.buildRequired("port", "端口"));
        params.add(ParamMeta.buildRequired("username", "用户名"));
        params.add(ParamMeta.buildRequired("password", "密码"));
        params.add(ParamMeta.buildRequired("fromAddress", "发件人邮箱"));
        params.add(ParamMeta.buildRequired("smtpServer", "smtpServer"));
        params.add(ParamMeta.buildDefault("isSSL", "isSSL", "是否开启ssl(可选)", "true"));
        params.add(ParamMeta.buildDefault("isAuth", "isAuth", "是否启用认证(可选)", "true"));
    }

    @Override
    public List<ParamMeta> getConfigParams() {
        return params;
    }
}
