package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 寄存器点位
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_signal")
public class Signal extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 点位名称
     */
    private String name;

    /**
     * 点位地址
     */
    private String address;

    /**
     * 信号类型(1. 点位 2. 自定义报文)
     */
    private Integer type;

    /**
     * 读取的寄存器数量
     */
    private Integer num;

    /**
     * 自定义报文
     */
    private String message;

    /**
     * 报文编码(HEX, UTF8, ASCII)
     */
    private String encode;

    /**
     * 所属产品
     */
    private Long productId;

    /**
     * 点位字段名称(同一个产品下的字段唯一)
     */
    private String fieldName;

    /**
     * 协议指令
     */
    private String direct;

    private Date createTime;

    /**
     * 所属协议
     */
    @TableField(exist = false)
    private Long protocolId;

    /**
     * 指令名称
     */
    @TableField(exist = false)
    private String directName;

    /**
     * 产品明此
     */
    @TableField(exist = false)
    private String productName;

    /**
     * 产品类型id
     */
    @TableField(exist = false)
    private Long productTypeId;

    public Signal() { }
}
