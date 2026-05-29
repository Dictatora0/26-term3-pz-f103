package com.iteaj.iboot.module.iot.dto;

import lombok.Data;

@Data
public class CurrentDeviceDto {

    /**
     * 总设备数量
     */
    private Integer today;

    /**
     * 在线数量
     */
    private Integer online;
}
