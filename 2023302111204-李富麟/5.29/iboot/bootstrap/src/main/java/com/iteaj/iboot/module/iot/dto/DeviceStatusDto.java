package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceStatusDto {

    /**
     * 在线数量
     */
    private int onlineNum;

    /**
     * 离线数量
     */
    private int offlineNum;
}
