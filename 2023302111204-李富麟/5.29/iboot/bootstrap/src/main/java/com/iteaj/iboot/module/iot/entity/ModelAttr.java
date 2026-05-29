package com.iteaj.iboot.module.iot.entity;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.spi.iot.consts.AttrType;
import com.iteaj.framework.validate.AllGroup;
import com.iteaj.iboot.module.iot.consts.AttrOrigin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 物模型属性
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_model_attr")
public class ModelAttr extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属产品
     */
    @NotNull(message = "所属产品必填")
    private Long productId;

    /**
     * 属性代码
     */
    @TableField(condition = SqlCondition.LIKE)
    @NotBlank(message = "属性字段必填")
    private String field;

    /**
     * 属性名称
     */
    @TableField(condition = SqlCondition.LIKE)
    @NotBlank(message = "属性名称必填")
    private String name;

    /**
     * 数据类型
     */
    @NotBlank(message = "数据类型必填")
    private String dataType;

    /**
     * 实际类型
     */
    private String realType;

    /**
     * 读写方式
     */
    private AttrType attrType;

    /**
     * 是否枚举
     */
    private Boolean enumerate;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 精度
     */
    private Integer accuracy;

    /**
     * 数据增益
     */
    private Integer gain;

    /**
     * 数据解析器
     */
    private String resolver;

    /**
     * 自定义解析脚本
     */
    private String script;

    /**
     * 描述
     */
    private String remark;

    /**
     * 属性来源
     */
    @NotNull(message = "属性来源必填")
    private AttrOrigin origin;

    /**
     * 是否属于控制状态
     */
    private Boolean ctrlStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 属性字典列表
     */
    @TableField(exist = false)
    private List<ModelAttrDict> dicts;

    public ModelAttrDict resolveValueToDict(String value) {
        if(CollectionUtil.isNotEmpty(dicts)) {
            for (int i = 0; i < dicts.size(); i++) {
                ModelAttrDict attrDict = dicts.get(i);
                if(attrDict.getDictValue().equals(value)) {
                    return attrDict;
                }
            }
        }

        return null;
    }
}
