package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.spi.iot.consts.CollectStatus;
import com.iteaj.framework.spi.iot.data.RealtimeCollectData;
import com.iteaj.iboot.module.iot.consts.CollectMode;
import com.iteaj.iot.tools.annotation.IotField;
import com.iteaj.iot.tools.annotation.IotTable;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("iot_collect_data")
@IotTable("iot_collect_data")
public class CollectData extends BaseEntity implements RealtimeCollectData {

    /**
     * 任务执行标识
     */
    @IotField
    private String cid;

    /**
     * 设备uid
     */
    @IotField
    private String uid;

    /**
     * 信号id
     */
    @IotField("signal_id")
    private Long signalId;

    /**
     * 属性
     */
    @IotField("field")
    private String field;

    @TableField(exist = false)
    private String fieldName;

    /**
     * 采集任务标识
     */
    @IotField("collect_task_id")
    private Long collectTaskId;

    /**
     * 采集地址
     */
    @IotField
    private String address;

    /**
     * 采集值
     */
    @IotField
    private String value;

    /**
     * 采集状态
     */
    @IotField
    private Boolean status;

    /**
     * 采集状态说明
     */
    @IotField
    private String reason;

    /**
     * 采集方式
     */
    @IotField(value = "collect_mode")
    private String collectMode;

    /**
     * 采集时间
     */
    @IotField(value = "collect_time", type = 93)
    private Date collectTime;

    /**
     * 创建时间
     */
    @IotField(value = "create_time", type = 93)
    private Date createTime;

    /**
     * 采集状态
     */
    private CollectStatus collectStatus;

    public CollectData() { }

    public CollectData(CollectMode collectMode) {
        this.collectMode = collectMode.name();
    }

    public CollectData setCollectStatus(CollectStatus collectStatus) {
        this.collectStatus = collectStatus;
        if(collectStatus == CollectStatus.Success) {
            this.status = true;
        } else {
            this.status = false;
            this.reason = collectStatus.getReason();
        }

        return this;
    }

    @Override
    public Long getFieldId() {
        return getSignalId();
    }

    @Override
    public Long getTaskId() {
        return getCollectTaskId();
    }

    @Override
    public String getProtocolCode() {
        return null;
    }

    @Override
    public String getProductCode() {
        return null;
    }

    @Override
    public Long getDeviceGroupId() {
        return null;
    }
}
