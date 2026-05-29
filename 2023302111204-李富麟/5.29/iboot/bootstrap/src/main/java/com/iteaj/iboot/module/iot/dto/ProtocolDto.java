package com.iteaj.iboot.module.iot.dto;

import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.iteaj.iboot.module.iot.entity.ProtocolApi;
import com.iteaj.iboot.module.iot.entity.ProtocolAttr;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProtocolDto extends Protocol {

    /**
     * 绑定的设备数量
     */
    private Long deviceCount;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品代码
     */
    private String productCode;

    /**
     * 设备配置
     */
    private ParamMeta[] deviceConfig;

    /**
     * 产品配置
     */
    private ParamMeta[] productConfig;

    /**
     * 网关配置
     */
    private ParamMeta[] gatewayConfig;

    /**
     * 协议属性列表
     */
    private List<ProtocolAttr> attrs = new ArrayList<>(16);

    /**
     * 协议功能接口列表
     */
    private List<ProtocolApi> funcApis = new ArrayList<>(16);

    /**
     * 协议事件接口列表
     */
    private List<ProtocolApi> eventApis = new ArrayList<>(16);

    public ProtocolDto addApis(ProtocolApi api) {
        if(api.getType() == ProtocolApiType.event) {
            this.eventApis.add(api);
        } else if(api.getType() == ProtocolApiType.func) {
            this.funcApis.add(api);
        } else {

        }

        return this;
    }
}
