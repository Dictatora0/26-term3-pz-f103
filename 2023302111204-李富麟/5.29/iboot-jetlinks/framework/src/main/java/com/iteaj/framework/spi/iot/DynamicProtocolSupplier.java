package com.iteaj.framework.spi.iot;

import com.iteaj.iot.FrameworkComponent;

import java.util.Objects;

/**
 * 动态协议实现
 */
public abstract class DynamicProtocolSupplier<C extends NetworkConfigImpl, T extends FrameworkComponent> implements DeviceProtocolSupplier<C, T> {

    @Override
    public boolean equals(Object obj) {
        DeviceProtocolSupplier supplier = (DeviceProtocolSupplier) obj;
        return Objects.equals(supplier.getProtocol().getCode(), this.getProtocol().getCode());
    }
}
