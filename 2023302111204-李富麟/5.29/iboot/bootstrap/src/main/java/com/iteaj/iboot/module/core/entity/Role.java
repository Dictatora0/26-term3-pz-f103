package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.iboot.module.core.enums.Status;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * create time: 2019/11/27
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@TableName("sys_role")
@Accessors(chain = true)
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     *  状态
     */
    private Status status;
    /**
     * 备注
     */
    private String remark;

    /**创建时间*/
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createTime;

    /**更新时间*/
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date updateTime;
}
