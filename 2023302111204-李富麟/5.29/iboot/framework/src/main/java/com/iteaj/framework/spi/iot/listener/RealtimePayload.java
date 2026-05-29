package com.iteaj.framework.spi.iot.listener;

import java.util.Collection;

/**
 * 实时负载
 * @see IotEventType#Realtime
 * @see RealtimeInfoListener
 */
public class RealtimePayload extends EntityPayload<Collection> {

    /**
     * 实时负载类型
     * warn 实时告警
     * status 实时设备在线状态
     * data 实时采集的数据
     */
    private String type;

    public RealtimePayload(String type, Collection values) {
        super(values);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
