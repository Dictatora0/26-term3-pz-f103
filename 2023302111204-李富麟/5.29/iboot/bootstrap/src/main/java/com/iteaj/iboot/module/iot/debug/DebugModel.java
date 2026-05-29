package com.iteaj.iboot.module.iot.debug;

import lombok.Data;

@Data
public class DebugModel {

    /**
     * 记录id
     */
    private Long id;

    /**
     * 自定义报文
     */
    private String message;

    /**
     * 设备编号
     */
    private String deviceSn;
}
