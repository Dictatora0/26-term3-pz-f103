package com.iteaj.framework.spi.iot.consts;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.NetworkConfigImpl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 传输协议
 */
public enum TransportProtocol {

    TCP("TCP协议", NetworkConfigImpl.TcpNetworkConfig.tcpMetas, false),
    UDP("UDP协议", NetworkConfigImpl.UdpNetworkConfig.udpMetas, true),
    MQTT("MQTT协议", NetworkConfigImpl.MqttNetworkConfig.mqttMetas, false),
    HTTP("HTTP协议", NetworkConfigImpl.HttpNetworkConfig.httpMetas, false),
    SERIAL("SERIAL协议", NetworkConfigImpl.SerialConfig.serialMetas, false),
    WEBSOCKET("WEBSOCKET协议", NetworkConfigImpl.WebSocketConfig.websocketMetas, true),
    ;

    private String desc;
    /**
     * 是否开放
     */
    private boolean disabled;
    private ParamMeta[] metas;

    TransportProtocol(String desc, ParamMeta[] metas, boolean disabled) {
        this.desc = desc;
        this.metas = metas;
        this.disabled = disabled;
    }

    public String getDesc() {
        return desc;
    }

    public ParamMeta[] getMetas() {
        return metas;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public static List<IVOption> options() {
        return Arrays.stream(TransportProtocol.values())
                .filter(item -> !item.disabled)
                .map(item -> new IVOption(item.getDesc(), item.name(), item.isDisabled(), item.getMetas()))
                .collect(Collectors.toList());
    }
}
