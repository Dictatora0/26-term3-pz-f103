package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 用于存储设备信息
 */
@Mapper
@Repository("wvpDeviceMapper")
public interface DeviceMapper extends BaseMapper<Device> {

    Device getDeviceByDeviceId(String deviceId);

    Page<Device> getDevices(Page page, Boolean onLine);

    Page<Device> detailByPage(Page page, Device entity);
}
