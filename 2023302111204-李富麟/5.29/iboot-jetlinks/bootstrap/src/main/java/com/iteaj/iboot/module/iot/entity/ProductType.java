package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.TreeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 产品类型
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_product_type")
public class ProductType extends TreeEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 父节点
     */
    private Long pid;

    /**
     * 路径
     */
    private String path;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 父类型名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 类型别名
     */
    private String alias;

    /**
     * 类型说明
     */
    private String remark;

    private Date createTime;
}
