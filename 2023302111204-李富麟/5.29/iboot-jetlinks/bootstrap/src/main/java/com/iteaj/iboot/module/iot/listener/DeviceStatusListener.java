package com.iteaj.iboot.module.iot.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.spi.event.ApplicationReadyListener;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.listener.IotEventType;
import com.iteaj.framework.spi.iot.listener.IotListener;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.utils.IotLogger;
import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.event.CombinedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 设备上下线状态事件监听
 */
@Component
public class DeviceStatusListener implements CombinedEventListener, ApplicationReadyListener, IotListener<RealtimeStatus> {

    private final IotCacheManager cacheManager;
    private final IDeviceService deviceService;
    private final FrameworkProperties properties;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public DeviceStatusListener(IotCacheManager cacheManager, IDeviceService deviceService, FrameworkProperties properties) {
        this.cacheManager = cacheManager;
        this.deviceService = deviceService;
        this.properties = properties;
    }

    @Override
    public void online(String source, FrameworkComponent component) {
        // 设备上线事件
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(component.getMessageClass());
        if(supplier != null) {
            RealtimeStatus device = cacheManager.get(supplier.getProtocol().getCode(), source);
            if(device != null) {
                device.setStatus(DeviceStatus.online);
                deviceService.updateDeviceStatus(device);
            } else {
                IotLogger.warn(DeviceStatus.online, null, "设备上线 更新失败(设备不存在) - 设备编号: {} - 驱动: {}", source, supplier.getDesc());
            }
        } else {
            IotLogger.warn(DeviceStatus.online, null, "设备上线 更新失败(未找到驱动) - 设备编号: {} - 协议类: {}", source, component.getMessageClass().getSimpleName());
        }
    }

    @Override
    public void offline(String source, FrameworkComponent component) {
        // 设备掉线事件
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(component.getMessageClass());
        if(supplier != null) {
            RealtimeStatus device = cacheManager.get(supplier.getProtocol().getCode(), source);
            if(device != null) {
                device.setStatus(DeviceStatus.offline);
                deviceService.updateDeviceStatus(device);
            } else {
                IotLogger.warn(DeviceStatus.offline, null, "设备离线 更新失败(设备不存在) - 设备编号: {} - 驱动: {}", source, supplier.getDesc());
            }
        } else {
            IotLogger.warn(DeviceStatus.online, null, "设备离线 更新失败(未找到驱动) - 设备编号: {} - 协议类: {}", source, component.getMessageClass().getSimpleName());
        }
    }

    @Override
    public void started(ApplicationContext context) {
        if(!properties.isCluster()) {
            // 系统重启时 更新所有的设备状态到离线
            deviceService.update(Wrappers.<Device>lambdaUpdate()
                    .set(Device::getStatus, DeviceStatus.offline)
                    .set(Device::getSwitchTime, new Date()));
        }
    }

    @Override
    public boolean isEventMatcher(IotEventType eventType) {
        return IotEventType.DeviceStatus == eventType;
    }

    @Override
    public void onIotEvent(IotEventType eventType, RealtimeStatus value) {
        deviceService.updateDeviceStatus(value);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1000;
    }
}
