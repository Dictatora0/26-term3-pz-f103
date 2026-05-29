package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 岗位管理
 * </p>
 *
 * @author iteaj
 * @since 2023-09-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_post")
public class Post extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 所属机构
     */
    private Long orgId;

    /**
     * 岗位介绍
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 机构名称
     */
    @TableField(exist = false)
    private String orgName;
}
