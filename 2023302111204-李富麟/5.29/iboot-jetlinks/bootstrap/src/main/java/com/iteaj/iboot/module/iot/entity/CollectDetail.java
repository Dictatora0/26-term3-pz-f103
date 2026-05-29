package com.iteaj.iboot.module.iot.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.dto.DeviceDto;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@TableName("iot_collect_detail")
public class CollectDetail extends BaseEntity {

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 存储设备uid
     */
    private Long storeUid;

    /**
     * 存储动作
     */
    private String storeAction;

    /**
     * 采集任务id
     */
    private Long collectTaskId;

    /**
     * 点位组
     */
    private Long pointGroupId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 点位组点位数量
     */
    @TableField(exist = false)
    private Integer signalNum;

    /**
     * 点位组名称
     */
    @TableField(exist = false)
    private String pointGroupName;

    /**
     * 任务名称
     */
    @TableField(exist = false)
    private String collectTaskName;

    /**
     * 采集的设备列表
     */
    @TableField(exist = false)
    private List<CollectDevice> devices;

    public Map<String, Object> resolveConfig() {
        if(StringUtils.hasText(this.getExtend())) {
            try {
                return JSONObject.parseObject(this.getExtend());
            } catch (Exception e) {
                throw new ServiceException("配置必须是标准的json格式");
            }
        } else {
            return new HashMap<>();
        }
    }
}
