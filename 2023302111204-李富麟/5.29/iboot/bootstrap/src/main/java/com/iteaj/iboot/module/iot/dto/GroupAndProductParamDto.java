package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupAndProductParamDto {

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备分组id
     */
    private Long groupId;
}
