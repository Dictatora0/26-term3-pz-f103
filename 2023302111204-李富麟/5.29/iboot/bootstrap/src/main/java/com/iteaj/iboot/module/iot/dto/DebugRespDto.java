package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iot.consts.ExecStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DebugRespDto {

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
     * 失败原因
     */
    private String reason;

    /**
     * 执行状态
     */
    private ExecStatus status;

    public DebugRespDto() { }

    public DebugRespDto(String deviceSn) {
        this(deviceSn, System.currentTimeMillis());
    }

    public DebugRespDto(String deviceSn, long reqTime) {
        this.deviceSn = deviceSn;
        this.reqTime = reqTime;
    }
}
