package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.spi.admin.DictResource;
import com.iteaj.iboot.module.core.enums.Status;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * jdk：1.8
 *
 * @author iteaj
 * create time：2019/7/13
 */
@Data
@Accessors(chain = true)
@TableName("sys_dict_type")
public class DictType extends BaseEntity implements DictResource {

    /**
     * 字典类型
     */
    @TableField(condition = SqlCondition.LIKE)
    private String type;

    /**
     * 类型名称
     */
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 类型状态
     */
    private Status status;

    /**
     * 备注
     */
    private String remark;

    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;
}
