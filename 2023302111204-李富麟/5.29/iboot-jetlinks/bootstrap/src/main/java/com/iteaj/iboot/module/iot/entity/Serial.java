package com.iteaj.iboot.module.iot.entity;

import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.iteaj.iboot.module.iot.consts.SerialStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 串口设备
 * </p>
 *
 * @author iteaj
 * @since 2023-04-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_serial")
public class Serial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 串口
     */
    private String com;

    /**
     * 波特率
     */
    private Integer baudRate;

    /**
     * 数据位
     */
    private Integer dataBits;

    /**
     * 校验位
     */
    private Integer parity;

    /**
     * 停止位
     */
    private Integer stopBits;

    /**
     * 状态
     */
    private SerialStatus status;

    private Date createTime;


}
