package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 子设备
 * </p>
 *
 * @author iteaj
 * @since 2022-06-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_device_child")
public class DeviceChild extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 子设备名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 设备uid
     */
    private Long uid;

    /**
     * 子设备编号
     */
    private String childSn;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 备注
     */
    private String remark;

    private Date createTime;

}
