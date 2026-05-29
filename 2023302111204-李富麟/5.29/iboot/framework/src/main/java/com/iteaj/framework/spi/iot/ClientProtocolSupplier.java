package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.client.ClientComponent;
import com.iteaj.iot.client.IotClient;

public interface ClientProtocolSupplier<C extends NetworkConfig, T extends ClientComponent, K extends IotClient> extends DeviceProtocolSupplier<C, T> {

    /**
     * 创建Iot客户端
     * @param config 配置
     * @return 如果已经存在直接返回, 如果不存在则创建一个新的客户端
     */
    K createClient(C config);

    /**
     * 移除客户端
     * @param clientKey 客户端标识
     * @return 客户端
     */
    K removeClient(Object clientKey);

    @Override
    default ConnectionType getConnectionType() {
        return ConnectionType.Client;
    }
}
