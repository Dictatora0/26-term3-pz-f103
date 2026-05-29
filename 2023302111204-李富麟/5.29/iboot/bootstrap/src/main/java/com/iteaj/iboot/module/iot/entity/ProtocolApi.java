package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 协议接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_protocol_api")
public class ProtocolApi extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口类型
     */
    private ProtocolApiType type;

    /**
     * 事件触发方式
     */
    private TriggerMode triggerMode;

    /**
     * 接口代码
     */
    private String code;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口状态
     */
    private String status;

    /**
     * 接口说明
     */
    private String remark;

    /**
     * 功能类型
     */
    private FuncType funcType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 所属协议
     */
    private String protocolCode;

    /**
     * 接口上行配置列表
     */
    @TableField(exist = false)
    private List<ProtocolApiConfig> upConfigs;

    /**
     * 接口下行配置列表
     */
    @TableField(exist = false)
    private List<ProtocolApiConfig> downConfigs;
}
