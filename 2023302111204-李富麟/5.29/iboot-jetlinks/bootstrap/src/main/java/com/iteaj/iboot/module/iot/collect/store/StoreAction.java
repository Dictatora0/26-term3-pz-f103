package com.iteaj.iboot.module.iot.collect.store;

import com.iteaj.iboot.module.iot.collect.CollectException;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.entity.CollectDetail;

import java.util.List;
import java.util.Map;

public abstract class StoreAction {

    /**
     * 采集动作名称
     * @return
     */
    public abstract String getName();

    /**
     * 采集动作说明
     * @return
     */
    public abstract String getDesc();

    /**
     * 校验配置
     * @param jsonConfig
     */
    public abstract void configValidate(Map<String, Object> jsonConfig) throws CollectException;

    /**
     * 存储
     * @param detail
     * @param data
     */
    public abstract void store(CollectDetail detail, List<CollectData> data);
}
