package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.consts.TriggerMode;

import java.util.function.Consumer;

public interface ProtocolModelApi {

    /**
     * 协议接口代码
     * @return
     */
    String getCode();

    /**
     * 协议接口名称
     * @return
     */
    String getName();

    /**
     * 协议接口说明
     * @return
     */
    String getRemark();

    /**
     * 协议接口类型
     * @return
     */
    ProtocolApiType getType();

    /**
     * 触发方式
     * @return
     */
    TriggerMode getTriggerMode();

    /**
     * 所属协议模型
     * @return
     */
    ProtocolModel getProtocolModel();

    /**
     * 执行协议
     * @param arg
     * @param result
     */
    void invoke(ProtocolModelApiInvokeParam arg, Consumer<InvokeResult> result) throws ProtocolInvokeException;
}
