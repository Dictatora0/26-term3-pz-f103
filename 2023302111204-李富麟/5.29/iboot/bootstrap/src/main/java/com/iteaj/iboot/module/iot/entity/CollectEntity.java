package com.iteaj.iboot.module.iot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
public class CollectEntity {

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 设备uid
     */
    private Long uid;

    /**
     * 采集id
     * 每次定时采集任务都会生成唯一的标识
     */
    private Long cid;

    /**
     * 设备编号
     */
    private String deviceSn;

    /**
     * 采集值
     */
    private Object value;

    /**
     * 地址
     */
    private String address;

    /**
     * 信号id
     */
    private Long signalId;

    /**
     * 采集时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
}
