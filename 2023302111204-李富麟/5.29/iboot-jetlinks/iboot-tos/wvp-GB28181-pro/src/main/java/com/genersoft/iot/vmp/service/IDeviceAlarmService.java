package com.genersoft.iot.vmp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.DeviceAlarm;

import java.util.List;

/**
 * 报警相关业务处理
 */
public interface IDeviceAlarmService {

    /**
     * 根据多个添加获取报警列表
     * @param page 当前页
     * @param entity 设备告警
     * @return 报警列表
     */
    Page<DeviceAlarm> listByPage(Page page, DeviceAlarm entity);

    /**
     * 添加一个报警
     * @param deviceAlarm 添加报警
     */
    void add(DeviceAlarm deviceAlarm);

    /**
     * 清空时间以前的报警
     * @param id 数据库id
     * @param deviceIdList 制定需要清理的设备id
     * @param time 不写时间则清空所有时间的
     */
    int clearAlarmBeforeTime(Integer id, List<String> deviceIdList, String time);

}
