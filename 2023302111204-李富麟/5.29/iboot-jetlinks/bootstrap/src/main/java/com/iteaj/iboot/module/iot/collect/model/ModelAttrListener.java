package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectDataListener;

public interface ModelAttrListener extends CollectDataListener {

    /**
     * 模型属性发布
     * @param supplier
     * @param device
     */
    void supplier(DataSupplier supplier, RealtimeStatus device);
}
