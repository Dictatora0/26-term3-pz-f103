package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物模型属性字典
 */
@Data
@Accessors(chain = true)
@TableName("iot_model_attr_dict")
public class ModelAttrDict extends BaseEntity {

    /**
     * 路径
     */
    private String path;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称必填")
    @TableField(condition = SqlCondition.LIKE)
    private String dictName;

    /**
     * 字典值
     */
    @NotBlank(message = "字典值必填")
    private String dictValue;

    /**
     * 数据类型
     */
    @TableField(exist = false)
    private String dataType;

    /**
     * 属性字段
     */
    @TableField(exist = false)
    private String attrField;

    /**
     * 精度
     */
    @TableField(exist = false)
    private Integer accuracy;

    /**
     * 数据增益
     */
    @TableField(exist = false)
    private Integer gain;

    /**
     * 数据解析器
     */
    @TableField(exist = false)
    private String resolver;

    /**
     * 自定义解析脚本
     */
    @TableField(exist = false)
    private String script;

    /**
     * 模型属性id
     */
    @NotNull(message = "模型属性必填")
    @TableField(whereStrategy = FieldStrategy.IGNORED)
    private Long modelAttrId;
}
