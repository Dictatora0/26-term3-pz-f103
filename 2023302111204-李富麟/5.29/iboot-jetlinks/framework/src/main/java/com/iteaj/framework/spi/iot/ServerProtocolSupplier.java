package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.iot.server.ServerComponent;

public interface ServerProtocolSupplier<C extends NetworkConfig, T extends ServerComponent> extends DeviceProtocolSupplier<C, T> {

    /**
     * 关闭指定的设备连接
     * @param deviceSn 设备编号
     */
    boolean close(String deviceSn);

    default ConnectionType getConnectionType() {
        return ConnectionType.Server;
    }
}
