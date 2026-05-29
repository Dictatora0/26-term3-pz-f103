package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import com.iteaj.framework.mybatis.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 点位组
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_point_group")
public class PointGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 组名称
     */
    private String name;

    /**
     * 点位数量
     */
    private Integer signalNum;

    /**
     * 点位id
     */
    @TableField(exist = false)
    private Long signalId;

    /**
     * 产品id列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ArrayNode productIds;

    @TableField(exist = false)
    private String productNames;

    /**
     * 点位信号列表
     */
    @TableField(exist = false)
    private List<String> signalIds;

    @TableField(exist = false)
    private Long productId;

    @TableField(exist = false)
    private Long productTypeId;

    private Date createTime;

}
