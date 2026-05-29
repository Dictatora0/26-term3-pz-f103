package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EMapDeviceDto {

    /**
     * 设备uid
     */
    private String uid;

    /**
     * 所属产品
     */
    private Long productId;

    /**
     * 所属组
     */
    private Long deviceGroupId;

    /**
     * 设备编号
     */
    private String deviceSn;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备状态
     */
    private DeviceStatus status;

    /**
     * 设备所在地址
     */
    private String address;

    /**
     * 设备经度
     */
    private String lon;

    /**
     * 设备纬度
     */
    private String lat;

    /**
     * 设备图片
     */
    private String logo;
}
