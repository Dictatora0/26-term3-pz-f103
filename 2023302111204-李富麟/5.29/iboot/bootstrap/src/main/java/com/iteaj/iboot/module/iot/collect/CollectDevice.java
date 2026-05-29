package com.iteaj.iboot.module.iot.collect;

import com.iteaj.framework.spi.iot.DeviceKey;
import com.iteaj.iboot.module.iot.entity.Signal;
import lombok.Data;

import java.util.List;

/**
 * 采集设备
 */
@Data
public class CollectDevice {

    /**
     * 点位组id
     */
    private Long pointGroupId;

    /**
     * 设备uid
     */
    private Long uid;

    /**
     * 设备编号
     */
    private String deviceSn;

    /**
     * 父设备编号
     */
    private String parentDeviceSn;

    /**
     * 设备绑定的产品id
     */
    private String productId;

    /**
     * 产品代码
     */
    private String productCode;

    /**
     * 设备绑定的产品名称
     */
    private String productName;

    /**
     * 设备使用的协议代码
     */
    private String protocolCode;

    /**
     * 设备使用的协议
     */
    private String protocolName;

    /**
     * 采集的点位信息
     */
    private List<CollectSignal> signals;

    public DeviceKey buildKey() {
        return DeviceKey.build(getDeviceSn(), getParentDeviceSn());
    }
}
