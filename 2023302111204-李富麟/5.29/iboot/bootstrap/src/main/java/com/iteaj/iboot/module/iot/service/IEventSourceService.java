package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.*;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;
import com.iteaj.iboot.module.iot.dto.FuncStatusProfileDto;
import com.iteaj.iboot.module.iot.dto.GroupAndProductParamDto;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.framework.IBaseService;
import com.iteaj.iboot.module.iot.entity.Product;

import java.util.List;

/**
 * <p>
 * 事件源 服务类
 * </p>
 *
 * @author iteaj
 * @since 2024-02-27
 */
public interface IEventSourceService extends IBaseService<EventSource> {

    PageResult<IPage<EventSource>> detailPage(Page<EventSource> page, EventSource entity);

    DetailResult<EventSource> detailById(Long id);

    BooleanResult updateConfig(EventSource entity);

    ListResult<IVOption> availableSources(Long id);

    /**
     * 移除所有相关联的数据
     * @param idList
     * @return
     */
    BooleanResult removeAllByIds(List<Long> idList);

    BooleanResult switchStatus(FuncStatus status, Long id);

    DetailResult<EventSourceDto> collectDetailById(Long id);

    /**
     * 获取事件源绑定的组下面的所有产品
     * @param id
     * @param status
     * @return
     */
    ListResult<Product> listProductById(Long id, FuncStatus status);

    /**
     * 获取指定事件源下的设备组列表
     * @param id 事件源id
     * @return
     */
    ListResult<DeviceGroup> listDeviceGroupById(Long id);

    /**
     * 统计事件源下面功能状态信息
     * @param param
     * @return
     */
    FuncStatusProfileDto countStatusProfile(GroupAndProductParamDto param);
}
