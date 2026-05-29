package com.iteaj.framework.spi.iot;

import com.iteaj.iot.DeviceManager;
import com.iteaj.iot.server.ServerComponent;

public abstract class StaticServerProtocolSupplier<C extends NetworkConfigImpl, T extends ServerComponent> extends StaticProtocolSupplier<C, T> implements ServerProtocolSupplier<C, T>{

    @Override
    public boolean close(String deviceSn) {
        T component = getComponent();
        if (component != null) {
            DeviceManager deviceManager = component.getDeviceManager();
            if (deviceManager != null) {
                return deviceManager.close(deviceSn);
            }
        }

        return false;
    }

    @Override
    protected abstract void doRefresh(T component, C config);

}
