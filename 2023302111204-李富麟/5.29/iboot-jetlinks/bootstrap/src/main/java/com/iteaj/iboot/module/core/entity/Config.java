package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.iboot.module.core.enums.ConfigType;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * create time: 2019/12/4
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@TableName("sys_config")
@Accessors(chain = true)
public class Config extends BaseEntity {

    @TableField(condition = SqlCondition.LIKE)
    private String name;
    private String label;
    private String value;
    private String remark;
    private ConfigType type;

    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

    public Config() { }

    public Config(ConfigType type) {
        this.type = type;
    }

}
