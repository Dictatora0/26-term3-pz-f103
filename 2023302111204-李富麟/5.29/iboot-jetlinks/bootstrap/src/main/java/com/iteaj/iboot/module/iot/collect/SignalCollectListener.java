package com.iteaj.iboot.module.iot.collect;

import com.iteaj.iboot.module.iot.entity.CollectData;

import java.util.List;

public interface SignalCollectListener extends CollectDataListener {

    void supplier(CollectDevice device, CollectData data);

    default void finished(CollectDevice device, List<CollectData> data) {}
}
