package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.framework.IBaseService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 设备分组 服务类
 * </p>
 *
 * @author iteaj
 */
public interface IDeviceGroupService extends IBaseService<DeviceGroup> {

    ListResult<DeviceGroup> tree(DeviceGroup entity);

    boolean hasBindDevice(Serializable id);

    DetailResult<DeviceGroup> detailById(Long id);

    /**
     * 通过设备组id获取此记录对应组产品id的设备组
     * @param id 设备组id
     * @return
     */
    DetailResult<DeviceGroup> getGroupProductById(Long id);

    boolean hasBindEventSource(Serializable id);

    PageResult<IPage<DeviceGroup>> detailPage(Page<DeviceGroup> page, DeviceGroup entity);
}
