package com.iteaj.iboot.module.iot.cache.data;

import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.collect.SignalCollectListener;
import com.iteaj.iboot.module.iot.collect.model.CollectInfo;
import com.iteaj.iboot.module.iot.collect.model.EventGroupCollectListener;
import com.iteaj.iboot.module.iot.collect.model.ModelAttrListener;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.iboot.module.iot.utils.IotLogger;

public class RealtimeDataCacheListener implements EventGroupCollectListener, SignalCollectListener, ModelAttrListener {

    private final RealtimeDataService realtimeDataService;

    public RealtimeDataCacheListener(RealtimeDataService realtimeDataService) {
        this.realtimeDataService = realtimeDataService;
    }

    @Override
    public void supplier(DataSupplier supplier, RealtimeStatus device) {
        if(supplier != null) {
            realtimeDataService.put(supplier);
            IotLogger.debug(IotLogger.REPORT_NAME, supplier.getProtocolCode(), device.getUid()
                    , "<<< 接收到设备上报数据 设备编号: {}", supplier.getKey().getKey());
        }
    }

    @Override
    public void supplier(EventSource source, DeviceGroup group, CollectInfo.DeviceInfo info) {
        realtimeDataService.put(info.getSupplier());
    }

    @Override
    public void supplier(CollectDevice device, CollectData data) {
        realtimeDataService.put(device.getProtocolCode(), device.buildKey()
                , SignalOrFieldValue.build(data.getSignalId(), data.getAddress(), data.getCollectTime()
                        , data.getValue(), data.getAddress()).builder(data.getCollectStatus()));
    }
}
