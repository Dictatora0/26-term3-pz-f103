package com.iteaj.iboot.module.core.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.consts.UseStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 系统管理员
 */
@Data
@TableName("sys_admin")
@Accessors(chain = true)
public class Admin extends BaseEntity implements AuthenticationUser {

    /**
     * 性别
     */
    @Excel(name = "性别", orderNum = "60", dict = "core_sex")
    private String sex;

    /**
     * 所属部门
     */
    @Excel(name = "部门", orderNum = "30", dict = "org_id", width = 20)
    private Long orgId;

    /**
     * 所属岗位
     */
    private Long postId;

    /**
     * 用户昵称
     */
    @TableField(condition = SqlCondition.LIKE)
    @Excel(name = "用户昵称", orderNum = "20", width = 18)
    private String name;

    /**
     * 邮箱
     */
    @Excel(name = "邮箱", orderNum = "50", width = 20)
    private String email;

    /**
     * 手机号
     */
    @Excel(name = "手机号", orderNum = "40", width = 16)
    @TableField(condition = SqlCondition.LIKE)
    private String phone;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户账号
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    @Excel(name = "用户账号", orderNum = "10", width = 18)
    private String account;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 用户状态(enabled, disabled)
     */
    @Excel(name = "用户状态", orderNum = "70", replace = {"启用_enabled", "禁用_disabled"})
    private UseStatus status;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "90", exportFormat = "yyyy-MM-dd", width = 15)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd", timezone="GMT+8")
    private Date createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

    /**
     * 修改人
     */
    private String updateBy;

    @JsonIgnore
    public boolean isSuperUser() {
        return this.getId() == 1l;
    }

    @Override
    public boolean allowLogin() {
        return this.getStatus() == UseStatus.enabled;
    }
}

