package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.TreeEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * create time: 2019/11/26
 *  组织机构实体
 * @author iteaj
 * @since 1.0
 */
@Data
@TableName("sys_org")
@Accessors(chain = true)
public class Org extends TreeEntity {
    /**
     * 当前树节点的父节点
     */
    private Long pid;

    /**
     * 部门名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 部门负责人电话
     */
    @TableField(condition = SqlCondition.LIKE)
    private String phone;
    /**
     * 部门负责人
     */
    @TableField(condition = SqlCondition.LIKE)
    private String leader;
    /**
     * 部门路径
     * @ignore
     */
    private String path;
    /**
     * 部门等级
     */
    private Integer level;

    /**创建时间*/
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

}
