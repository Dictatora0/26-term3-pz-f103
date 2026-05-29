package com.iteaj.iboot.module.iot.collect;

import lombok.Data;

@Data
public class CollectSignal {

    /**
     * 点位id
     */
    private Long id;

    /**
     * 点位名称
     */
    private String name;

    /**
     * 点位地址
     */
    private String address;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 寄存器数量
     */
    private Integer num;

    /**
     * 协议指令
     */
    private String direct;

    /**
     * 所属产品
     */
    private Long productId;

    /**
     * 点位组id
     */
    private Long pointGroupId;
}
