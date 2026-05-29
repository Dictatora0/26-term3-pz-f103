package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.consts.DeviceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTypeCountDto {

    /**
     * 设备类型
     */
    private DeviceType type;

    /**
     * 在线数量
     */
    private Integer online;

    /**
     * 总设备数
     */
    private Integer total;

    public String getTypeName() {
        return this.getType() != null ? this.getType().getDesc() : "";
    }
}
