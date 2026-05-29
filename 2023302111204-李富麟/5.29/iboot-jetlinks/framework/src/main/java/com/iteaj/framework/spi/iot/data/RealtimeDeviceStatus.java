package com.iteaj.framework.spi.iot.data;

import java.util.Date;

public interface RealtimeDeviceStatus {

    /**
     * 获取设备唯一标识
     * @return
     */
    String getUid();

    /**
     * 获取设备编号
     * @return
     */
    String getDeviceSn();

    /**
     * 获取设备名称
     * @return
     */
    String getDeviceName();

    /**
     * 获取设备状态(online、offline)
     * @return
     */
    String getDeviceStatus();

    /**
     * 获取状态更新时间
     * @return
     */
    Date getUpdateTime();

    /**
     * 获取设备组id
     * @return
     */
    Long getDeviceGroupId();

    /**
     * 获取协议码
     * @return
     */
    String getProtocolCode();

    /**
     * 获取产品码
     * @return
     */
    String getProductCode();

    /**
     * 获取设备类型
     * @return
     */
    String getDeviceType();

    /**
     * 获取父设备编号
     * @return
     */
    String getParentDeviceSn();
}
