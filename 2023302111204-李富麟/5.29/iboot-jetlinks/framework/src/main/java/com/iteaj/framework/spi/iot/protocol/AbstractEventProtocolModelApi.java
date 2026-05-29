package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.consts.TriggerMode;

import java.util.Map;

/**
 * 事件协议接口
 */
public abstract class AbstractEventProtocolModelApi extends AbstractProtocolModelApi {

    public AbstractEventProtocolModelApi(String code, String name) {
        this(code, name, null, TriggerMode.active);
    }

    public AbstractEventProtocolModelApi(String code, String name, String remark) {
        this(code, name, remark, TriggerMode.active);
    }

    public AbstractEventProtocolModelApi(String code, String name, String remark, TriggerMode triggerMode) {
        super(code, name, remark, ProtocolApiType.event);
        this.setTriggerMode(triggerMode);
    }

    public AbstractEventProtocolModelApi(String code, String name, Map<String, ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        this(code, name, null, TriggerMode.active, upConfig, downConfig);
    }

    public AbstractEventProtocolModelApi(String code, String name, String remark, TriggerMode triggerMode, Map<String, ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        super(code, name, remark, ProtocolApiType.event, upConfig, downConfig);
        this.setTriggerMode(triggerMode);
    }

}
