package com.iteaj.iboot.plugin.code.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * create time: 2020/4/27
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@TableName("t_gen_module")
@Accessors(chain = true)
public class GenModule extends BaseEntity {

    private String msn;
    private String name;
    private String remark;
    private String project;
    private String account;
    private String dbName;
    private String password;
    private String tablePrefix;

    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;
}
