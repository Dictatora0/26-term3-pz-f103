package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.TreeEntity;
import com.iteaj.framework.spi.admin.MenuResource;
import com.iteaj.framework.spi.admin.MenuType;
import com.iteaj.iboot.module.core.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 菜单实体
 */
@Data
@TableName("sys_menu")
@Accessors(chain = true)
public class Menu extends TreeEntity implements MenuResource, Comparable<Menu> {

    /**当前树节点的父节点*/
    private Long pid;
    /**菜单地址*/
    private String url;
    /**菜单名称*/
    @TableField(condition = SqlCondition.LIKE)
    private String name;
    /**
     * 所属模块
     */
    private String msn;
    /**排序*/
    private Integer sort;
    /**图标*/
    private String icon;
    /**是否显示*/
    private Status status;
    /**权限列表*/
    private String perms;
    /**备注*/
    private String remark;
    /**菜单类型*/
    private MenuType type;
    /**打开方式*/
    private String target;
    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private Date updateTime;

    public Menu() {  }

    public String getUrl() {
        return url;
    }

    public Menu setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int compareTo(@NotNull Menu o) {
        return this.getSort() - o.getSort();
    }
}
