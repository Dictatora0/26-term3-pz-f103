package com.iteaj.iboot.module.iot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.framework.spi.iot.protocol.ProtocolModelAttr;
import com.iteaj.iboot.module.iot.consts.GatewayStatus;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import com.iteaj.iboot.module.iot.entity.Product;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductDto extends Product {

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 操作类型
     */
    private CtrlMode ctrlMode;

    /**
     * 网关状态
     */
    private GatewayStatus gatewayStatus;

    /**
     * 传输协议
     */
    private TransportProtocol transportProtocol;

    /**
     * 物模型属性
     */
    private List<ModelAttr> attrs;

    /**
     * 物模型功能接口
     */
    private List<ModelApi> funcApis;

    /**
     * 物模型事件接口
     */
    private List<ModelApi> eventApis;

    /**
     * 协议码列表
     */
    @JsonIgnore
    private String[] protocolCodes;

    /**
     * 操作类型名称
     * @return
     */
    public String getCtrlModelName() {
        return this.ctrlMode != null ? this.ctrlMode.getDesc() : null;
    }

    /**
     * 传输协议名称
     * @return
     */
    public String getTransportProtocolName() {
        return this.transportProtocol != null ? this.transportProtocol.getDesc() : null;
    }
}
