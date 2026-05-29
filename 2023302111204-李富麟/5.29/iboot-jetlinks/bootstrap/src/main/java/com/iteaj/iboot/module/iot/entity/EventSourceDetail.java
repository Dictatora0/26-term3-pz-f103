package com.iteaj.iboot.module.iot.entity;

import com.iteaj.framework.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 事件源详情
 * </p>
 *
 * @author iteaj
 * @since 2024-02-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_event_source_detail")
public class EventSourceDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属事件源
     */
    private Long eventSourceId;

    /**
     * 物模型接口
     */
    private Long modelApiId;

    private Date createTime;


}
