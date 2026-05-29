package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.consts.TriggerMode;

import java.util.Map;

/**
 * 功能协议接口
 */
public abstract class AbstractFuncProtocolModelApi extends AbstractProtocolModelApi{

    private FuncType funcType;

    public AbstractFuncProtocolModelApi(String code, String name) {
        this(code, name, FuncType.W, TriggerMode.passive, null);
    }

    public AbstractFuncProtocolModelApi(String code, String name, String remark) {
        this(code, name, FuncType.W, TriggerMode.passive, remark);
    }

    public AbstractFuncProtocolModelApi(String code, String name, FuncType type) {
        this(code, name, type, TriggerMode.passive, null);
    }

    public AbstractFuncProtocolModelApi(String code, String name, FuncType type, TriggerMode mode, String remark) {
        super(code, name, remark, ProtocolApiType.func);
        this.funcType = type;
        this.setTriggerMode(mode);
    }

    public AbstractFuncProtocolModelApi(String code, String name, FuncType type, Map<String, ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        this(code, name, type, TriggerMode.passive, null, upConfig, downConfig);
    }

    public AbstractFuncProtocolModelApi(String code, String name, FuncType type, TriggerMode mode, String remark, Map<String, ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        super(code, name, remark, ProtocolApiType.func, upConfig, downConfig);
        this.funcType = type;
        this.setTriggerMode(mode);
    }

    public AbstractFuncProtocolModelApi buildRead() {
        return this.setFuncType(FuncType.R);
    }

    public AbstractFuncProtocolModelApi buildWrite() {
        return this.setFuncType(FuncType.W);
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public AbstractFuncProtocolModelApi setFuncType(FuncType funcType) {
        this.funcType = funcType; return this;
    }
}
