package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.iot.Protocol;
import com.iteaj.iot.consts.ExecStatus;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 协议模型接口
 */
public abstract class AbstractProtocolModelApi implements ProtocolModelApi{

    /**
     * 接口代码
     */
    private String code;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口说明
     */
    private String remark;

    /**
     * 接口类型
     */
    private ProtocolApiType type;

    /**
     * 触发方式
     */
    private TriggerMode triggerMode;

    /**
     * 所属协议模型
     */
    private ProtocolModel protocolModel;

    /**
     * 上行接口配置
     * @see java.util.LinkedHashMap<String, ProtocolModelApiConfig>
     * @see ProtocolModelApiConfig#getProtocolModelAttrField()
     */
    private Map<String, ProtocolModelApiConfig> upConfig;

    /**
     * 下行接口配置
     * @see java.util.LinkedHashMap<String, ProtocolModelApiConfig>
     * @see ProtocolModelApiConfig#getProtocolModelAttrField()
     */
    private Map<String, ProtocolModelApiConfig> downConfig;

    public static final String INVOKE_API = "_InvokeApi";
    public static final String INVOKE_API_VALUE = "_InvokeApiValue";
    public static final String INVOKE_API_DATA_SUPPLIER = "_InvokeApiDataSupplier";

    public AbstractProtocolModelApi(String code, String name, ProtocolApiType type) {
        this(code, name, type, new ProtocolLinkedMap<>(), new ProtocolLinkedMap<>());
    }

    public AbstractProtocolModelApi(String code, String name, String remark, ProtocolApiType type) {
        this(code, name, remark, type, new ProtocolLinkedMap<>(), new ProtocolLinkedMap<>());
    }

    public AbstractProtocolModelApi(String code, String name, ProtocolApiType type, Map<String
            , ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.upConfig = upConfig;
        this.downConfig = downConfig;
    }

    public AbstractProtocolModelApi(String code, String name, String remark, ProtocolApiType type
            , Map<String, ProtocolModelApiConfig> upConfig, Map<String, ProtocolModelApiConfig> downConfig) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.remark = remark;
        this.upConfig = upConfig;
        this.downConfig = downConfig;
    }

    @Override
    public String getCode() {
        return code;
    }

    public AbstractProtocolModelApi setCode(String code) {
        this.code = code; return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public AbstractProtocolModelApi setName(String name) {
        this.name = name; return this;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    public AbstractProtocolModelApi setRemark(String remark) {
        this.remark = remark; return this;
    }

    @Override
    public ProtocolApiType getType() {
        return type;
    }

    @Override
    public ProtocolModel getProtocolModel() {
        return this.protocolModel;
    }

    @Override
    public void invoke(ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) throws ProtocolInvokeException {
        Protocol protocol = buildProtocol(arg);
        if(protocol != null) {
            protocol.addParam(INVOKE_API, arg);
            arg.addConfig(ProtocolModelApiInvokeParam.INVOKE_API, this);
        }

        doInvoke(protocol, arg, invokeResult -> {
            // 执行成功则保存之后后的值
            if(invokeResult.getStatus() == ExecStatus.success) {
                protocol.addParam(INVOKE_API_VALUE, invokeResult.getValue());
            }

            result.accept(invokeResult);
        });
    }

    /**
     * 构建请求协议
     * @param arg
     * @return
     */
    protected abstract Protocol buildProtocol(ProtocolModelApiInvokeParam arg);

    /**
     * 调用协议
     * @param protocol
     * @param arg
     * @param result
     */
    protected abstract void doInvoke(Protocol protocol, ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result);

    /**
     * 新增上行接口配置
     * @param apiConfig
     * @return
     */
    public AbstractProtocolModelApi addUpConfig(ProtocolModelApiConfig apiConfig) {
        this.getUpConfig().put(apiConfig.getProtocolModelAttrField(), apiConfig);
        return this;
    }

    /**
     * 新增下行接口配置
     * @param apiConfig
     * @return
     */
    public AbstractProtocolModelApi addDownConfig(ProtocolModelApiConfig apiConfig) {
        this.getDownConfig().put(apiConfig.getProtocolModelAttrField(), apiConfig);
        return this;
    }

    public AbstractProtocolModelApi setType(ProtocolApiType type) {
        this.type = type; return this;
    }

    public Map<String, ProtocolModelApiConfig> getUpConfig() {
        return upConfig;
    }

    public void setUpConfig(Map<String, ProtocolModelApiConfig> upConfig) {
        this.upConfig = upConfig;
    }

    public Map<String, ProtocolModelApiConfig> getDownConfig() {
        return downConfig;
    }

    public TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(TriggerMode triggerMode) {
        this.triggerMode = triggerMode;
    }

    public void setDownConfig(Map<String, ProtocolModelApiConfig> downConfig) {
        this.downConfig = downConfig;
    }

    protected void setProtocolModel(ProtocolModel protocolModel) {
        this.protocolModel = protocolModel;
    }
}
