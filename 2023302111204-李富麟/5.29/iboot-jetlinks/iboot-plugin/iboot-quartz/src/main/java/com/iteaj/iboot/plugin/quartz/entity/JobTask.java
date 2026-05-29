package com.iteaj.iboot.plugin.quartz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("qrtz_job_task")
public class JobTask extends BaseEntity {

    /**
     * 作业名称
     */
    private String name;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 作业状态
     */
    private String status;

    /**
     * 作业备注
     */
    private String remark;

    /**
     * 执行的方法: <hr>
     *     spring的bean名+method e.g: task.test
     */
    private String method;

    /**
     * 执行参数
     */
    private String params;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * 是否并发
     */
    private Boolean concurrent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
