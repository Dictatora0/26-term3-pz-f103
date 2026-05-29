package com.iteaj.iboot.plugin.message.service;

import cn.hutool.json.JSONObject;
import org.dromara.sms4j.aliyun.config.AlibabaFactory;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;

import java.util.Objects;

public class AlibabaSmsService extends Sms4jMessageService {

    @Override
    public String getChannelId() {
        return SupplierConstant.ALIBABA;
    }

    @Override
    public String getChannelName() {
        return "阿里云";
    }

    @Override
    protected boolean isSuccess(SmsResponse response) {
        Object data = response.getData();
        if(data instanceof JSONObject) {
            return Objects.equals(((JSONObject) data).getStr("Code"), "OK");
        }

        return false;
    }

    @Override
    protected BaseProviderFactory buildBaseProviderFactory() {
        return AlibabaFactory.instance();
    }
}
