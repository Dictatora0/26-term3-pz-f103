package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IVOption;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;
import com.iteaj.iboot.module.iot.dto.FuncStatusProfileDto;
import com.iteaj.iboot.module.iot.dto.GroupAndProductParamDto;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.iot.entity.Product;

import java.util.List;

/**
 * <p>
 * 事件源 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2024-02-27
 */
public interface EventSourceMapper extends BaseMapper<EventSource> {

    IPage<EventSource> detailPage(Page<EventSource> page, EventSource entity);

    EventSource detailById(Long id);

    List<IVOption> availableSources(Long id);

    int removeAllByIds(List<Long> idList);

    EventSourceDto collectDetailById(Long id);

    List<Product> listProductById(Long id, FuncStatus status);

    List<DeviceGroup> listDeviceGroupById(Long id);

    /**
     * 统计事件源下面功能状态信息
     * @param param
     * @return
     */
    FuncStatusProfileDto countStatusProfile(GroupAndProductParamDto param);
}
