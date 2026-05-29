package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;

import java.util.Map;

public interface ProtocolModel {

    /**
     * 协议代码
     * @return
     */
    String getCode();

    /**
     * 协议说明
     * @return
     */
    String getRemark();

    /**
     * 协议控制方式
     * @return
     */
    CtrlMode getCtrlMode();

    /**
     * 使用的传输协议
     * @return
     */
    TransportProtocol getType();

    /**
     * 获取指定属性
     * @param filed
     * @return
     */
    ProtocolModelAttr getAttr(String filed);

    /**
     * 协议模型属性
     * @see java.util.LinkedHashMap<String, ProtocolModelAttr>
     * @see ProtocolModelAttr#getField() key
     * @return
     */
    Map<String, ProtocolModelAttr> getAttrs();

    /**
     * 根据接口代码获取接口
     * @param code
     * @return
     */
    AbstractProtocolModelApi getApi(String code);

    /**
     * 协议模型接口
     * @see java.util.LinkedHashMap<String,  AbstractProtocolModelApi >
     * @see AbstractProtocolModelApi#getCode() key
     * @return
     */
    Map<String, AbstractProtocolModelApi> getApis();
}
