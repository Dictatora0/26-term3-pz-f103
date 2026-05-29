package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.entity.CollectTask;
import lombok.Data;

import java.util.List;

@Data
public class CollectTaskDto extends CollectTask {

    /**
     * 点位组
     */
    private Long pointGroupId;

    /**
     * 点位列表
     */
    private List<CollectDetail> details;

}
