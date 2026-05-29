package com.iteaj.iboot.module.iot.debug;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DebugResult {

    /**
     * 设备编号
     */
    private String deviceSn;

    /**
     * 请求时间
     */
    private long reqTime;

    /**
     * 响应时间
     */
    private long respTime;

    /**
     * 读的值
     * 写为null
     */
    private Object value;

    /**
     * 请求的十六进制报文
     */
    private String reqMsg;

    /**
     * 响应的十六机制报文
     */
    private String respMsg;

    /**
     * 请求模型
     */
    private DebugModel model;

    public DebugResult(DebugModel model) {
        this.model = model;
    }
}
