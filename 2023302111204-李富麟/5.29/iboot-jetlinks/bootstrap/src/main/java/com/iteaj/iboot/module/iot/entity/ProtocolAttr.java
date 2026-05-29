package com.iteaj.iboot.module.iot.entity;

import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.iteaj.framework.spi.iot.consts.AttrType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 协议属性
 * </p>
 *
 * @author iteaj
 * @since 2023-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_protocol_attr")
public class ProtocolAttr extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 属性代码
     */
    private String field;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 属性说明
     */
    private String remark;

    /**
     * 读写方式
     */
    private AttrType attrType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 所属协议
     */
    private String protocolCode;

}
