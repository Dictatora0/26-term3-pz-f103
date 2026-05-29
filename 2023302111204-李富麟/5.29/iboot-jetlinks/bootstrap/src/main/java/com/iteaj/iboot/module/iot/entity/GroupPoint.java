package com.iteaj.iboot.module.iot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;

@Data
@TableName("iot_group_point")
public class GroupPoint extends BaseEntity {

    /**
     * 点位信号id
     */
    private Long signalId;

    /**
     * 点位组id
     */
    private Long groupId;

    public GroupPoint() { }

    public GroupPoint(Long signalId, Long groupId) {
        this.signalId = signalId;
        this.groupId = groupId;
    }
}
