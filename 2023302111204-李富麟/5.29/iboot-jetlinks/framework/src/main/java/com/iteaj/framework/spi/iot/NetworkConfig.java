package com.iteaj.framework.spi.iot;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.json.JacksonUtils;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;

import java.io.IOException;

public interface NetworkConfig {

    /**
     * 返回设备唯一id
     * @return uid
     */
    String getUid();

    /**
     * 主机地址
     * @return
     */
    String getHost();

    /**
     * 端口
     * @return
     */
    Integer getPort();

    /**
     * 是否启用ssl
     * @return
     */
    Boolean isUseSsl();

    /**
     * 返回设备编号
     * @return
     */
    String getDeviceSn();

    static NetworkConfig resolve(TransportProtocol protocol, JSONObject config) {
        if(protocol == TransportProtocol.TCP) {
            return config.toJavaObject(NetworkConfigImpl.TcpNetworkConfig.class);
        } else if(protocol == TransportProtocol.MQTT) {
            return config.toJavaObject(NetworkConfigImpl.MqttNetworkConfig.class);
        } else if(protocol == TransportProtocol.UDP) {
            return config.toJavaObject(NetworkConfigImpl.UdpNetworkConfig.class);
        } else if(protocol == TransportProtocol.HTTP) {
            return config.toJavaObject(NetworkConfigImpl.HttpNetworkConfig.class);
        } else if(protocol == TransportProtocol.WEBSOCKET) {
            return config.toJavaObject(NetworkConfigImpl.WebSocketConfig.class);
        } else if(protocol == TransportProtocol.SERIAL) {
            return config.toJavaObject(NetworkConfigImpl.SerialConfig.class);
        } else {
            throw new ServiceException("错误的协议类型["+protocol+"]");
        }
    }
}
