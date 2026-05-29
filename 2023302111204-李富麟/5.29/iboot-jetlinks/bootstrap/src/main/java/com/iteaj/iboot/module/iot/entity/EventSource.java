package com.iteaj.iboot.module.iot.entity;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import com.iteaj.framework.mybatis.handler.FastjsonTypeHandler;
import com.iteaj.iboot.module.iot.consts.EventSourceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 事件源
 * </p>
 *
 * @author iteaj
 * @since 2024-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_event_source")
public class EventSource extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     */
    private EventSourceType type;

    /**
     * 采集周期
     */
    private String cron;

    /**
     * 事件名称
     */
    private String name;

    /**
     * 场景状态
     */
    private FuncStatus status;

    /**
     * 有没有被动事件
     */
    private Boolean hasPassive;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 启用失败原因
     */
    private String reason;

    /**
     * 产品
     */
    @TableField(exist = false)
    private Long productId;

    /**
     * 设备组id
     */
    @TableField(exist = false)
    private Long deviceGroupId;

    /**
     * 操作的产品
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONArray productIds;

    /**
     * 设备组
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONArray deviceGroupIds;

    /**
     * 模型接口列表
     */
    @TableField(exist = false)
    private List<String> modelApiIds;
}
