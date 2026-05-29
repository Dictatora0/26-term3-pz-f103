package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import com.iteaj.framework.spi.iot.consts.ApiFieldType;
import com.iteaj.framework.spi.iot.protocol.ProtocolModelApiConfigOption;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 协议接口配置
 * </p>
 *
 * @author iteaj
 * @since 2023-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_protocol_api_config")
public class ProtocolApiConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 所属api接口
     */
    private String protocolApiCode;

    /**
     * 协议属性
     */
    private String protocolAttrField;

    /**
     * 备注
     */
    private String remark;

    /**
     * 点位地址
     */
    private String position;

    /**
     * 配置长度
     */
    private Integer length;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 属性名称
     */
    @TableField(exist = false)
    private String attrName;

    /**
     * 属性类型
     */
    @TableField(exist = false)
    private String dataType;

    /**
     * 属性说明
     */
    @TableField(exist = false)
    private String attrRemark;

    /**
     * 字段类型
     */
    private ApiFieldType fieldType;

    /**
     * 下拉款配置项
     */
    @TableField(exist = false)
    private List<ProtocolModelApiConfigOption> options;
}
