package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * <p>
 * 数据采集任务
 * </p>
 *
 * @author iteaj
 * @since 2022-08-28
 */
@Data
@Accessors(chain = true)
@TableName("iot_collect_task")
public class CollectTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;

    /**
     * 任务调度
     */
    @NotBlank(message = "任务调度不能为空")
    private String cron;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 采集任务说明
     */
    private String remark;

    /**
     * 任务失败的原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
