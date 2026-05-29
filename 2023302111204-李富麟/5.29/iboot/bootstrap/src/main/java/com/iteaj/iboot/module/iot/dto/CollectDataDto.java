package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.entity.CollectData;
import lombok.Data;

@Data
public class CollectDataDto extends CollectData {

    /**
     * 产品类型id
     */
    private Long productTypeId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备编号
     */
    private String deviceSn;

    /**
     * 设备名称
     */
    private String deviceName;
}
