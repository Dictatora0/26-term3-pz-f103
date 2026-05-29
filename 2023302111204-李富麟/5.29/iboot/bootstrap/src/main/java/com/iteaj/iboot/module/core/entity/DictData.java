package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.BaseEntity;
import com.iteaj.iboot.module.core.enums.Status;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * jdk：1.8
 *
 * @author iteaj
 * create time：2019/6/24
 */
@Data
@TableName("sys_dict_data")
public class DictData extends BaseEntity {

    /**
     * 排序
     */
    private Integer sort;
    /**
     * 字典标签
     */
    @TableField(condition = SqlCondition.LIKE)
    private String label;
    /**
     * 字典标签值
     */
    private String value;
    /**
     * 类型
     */
    private String type;
    /**
     * 字典状态
     */
    private Status status;
    /**
     * 备注
     */
    private String remark;

    private boolean izDefault;

    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

}
