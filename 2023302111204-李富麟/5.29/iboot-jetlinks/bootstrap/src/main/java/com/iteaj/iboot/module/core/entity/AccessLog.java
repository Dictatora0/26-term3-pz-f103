package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.logger.LoggerType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * create time: 2020/4/22
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("sys_access_log")
public class AccessLog extends BaseEntity {

    /**
     * 请求ip
     */
    private String ip;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 功能类型
     */
    private LoggerType type;

    /**
     * 访问用户
     */
    private Long userId;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 使用时间
     */
    @TableField(condition = "%s >= #{%s}")
    private Long millis;

    /**
     * 用户名
     */
    @TableField(condition = SqlCondition.LIKE)
    private String userName;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 错误消息
     */
    private String errMsg;

    /**
     * 功能
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createTime;

}
