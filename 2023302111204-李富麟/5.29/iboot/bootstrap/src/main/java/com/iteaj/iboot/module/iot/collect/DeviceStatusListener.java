package com.iteaj.iboot.module.iot.collect;

import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;

import java.util.Arrays;
import java.util.Collection;

public interface DeviceStatusListener extends CollectDataListener{

    default void supplier(RealtimeStatus device) {
        this.supplier(Arrays.asList(device));
    }

    default void supplier(Collection<RealtimeStatus> devices) {

    }
}
