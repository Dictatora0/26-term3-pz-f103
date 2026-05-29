package com.iteaj.iboot.module.iot.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SignalOfProductDto {

    private String id;

    /**
     * 点位名称
     */
    private String name;

    /**
     * 点位地址
     */
    private String address;

    /**
     * 点位字段名称
     */
    private String fieldName;

    /**
     * 产品名称
     */
    private String productName;
}
