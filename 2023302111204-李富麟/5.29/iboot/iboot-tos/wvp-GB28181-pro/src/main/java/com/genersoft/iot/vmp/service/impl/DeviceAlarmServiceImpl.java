package com.genersoft.iot.vmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.DeviceAlarm;
import com.genersoft.iot.vmp.service.IDeviceAlarmService;
import com.genersoft.iot.vmp.storager.dao.DeviceAlarmMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceAlarmServiceImpl implements IDeviceAlarmService {

    @Autowired
    private DeviceAlarmMapper deviceAlarmMapper;

    @Override
    public Page<DeviceAlarm> listByPage(Page page, DeviceAlarm entity) {
        return deviceAlarmMapper.selectPage(page, Wrappers.lambdaQuery(entity)
                .ge(StrUtil.isNotBlank(entity.getStartTime()), DeviceAlarm::getStartTime, entity.getStartTime())
                .le(StrUtil.isNotBlank(entity.getEndTime()), DeviceAlarm::getEndTime, entity.getEndTime()));
    }

    @Override
    public void add(DeviceAlarm deviceAlarm) {
        deviceAlarmMapper.insert(deviceAlarm);
    }

    @Override
    public int clearAlarmBeforeTime(Integer id, List<String> deviceIdList, String time) {
        return deviceAlarmMapper.clearAlarmBeforeTime(id, deviceIdList, time);
    }
}
