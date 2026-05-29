package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.iboot.module.iot.collect.CollectDataListener;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;

public interface EventGroupCollectListener extends CollectDataListener {

    /**
     *  组任务采集完成
     * @param info
     */
    default void finished(CollectInfo info) {}

    /**
     * 设备采集完成
     * @param source
     * @param group
     * @param info
     */
    void supplier(EventSource source, DeviceGroup group, CollectInfo.DeviceInfo info);
}
