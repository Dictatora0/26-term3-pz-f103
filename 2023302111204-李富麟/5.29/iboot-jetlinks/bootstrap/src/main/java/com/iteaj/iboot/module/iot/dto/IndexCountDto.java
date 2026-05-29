package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexCountDto {

    /**
     * 总设备数量
     */
    private Integer deviceNum;

    /**
     * 在线设备数量
     */
    private Integer onlineNum;

    /**
     * 总产品数量
     */
    private Integer productNum;

    /**
     * 启用的产品数量
     */
    private Integer enabledProductNum;

    /**
     * 事件源数量
     */
    private Integer eventSourceNum;

    /**
     * 运行中的事件源
     */
    private Integer runningEventSourceNum;

    /**
     * 协议数量
     */
    private Integer protocolNum;
}
