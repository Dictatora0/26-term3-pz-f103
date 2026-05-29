package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;
import com.iteaj.iboot.module.iot.dto.FuncStatusProfileDto;
import com.iteaj.iboot.module.iot.dto.GroupAndProductParamDto;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.iboot.module.iot.entity.EventSourceDetail;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.mapper.EventSourceMapper;
import com.iteaj.iboot.module.iot.service.IDeviceGroupService;
import com.iteaj.iboot.module.iot.service.IEventSourceDetailService;
import com.iteaj.iboot.module.iot.service.IEventSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 事件源 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2024-02-27
 */
@Service
public class EventSourceServiceImpl extends BaseServiceImpl<EventSourceMapper, EventSource> implements IEventSourceService {

    private final IDeviceGroupService deviceGroupService;
    private final IEventSourceDetailService eventSourceDetailService;

    public EventSourceServiceImpl(IDeviceGroupService deviceGroupService, IEventSourceDetailService eventSourceDetailService) {
        this.deviceGroupService = deviceGroupService;
        this.eventSourceDetailService = eventSourceDetailService;
    }

    @Override
    @Transactional
    public BooleanResult save(EventSource entity) {
        this.getOne(Wrappers.<EventSource>lambdaQuery()
                .eq(EventSource::getName, entity.getName()))
                .ifPresentThrow("名称已存在["+entity.getName()+"]");
        entity.setCreateTime(new Date());
        return super.save(entity);
    }

    @Override
    @Transactional
    public BooleanResult updateById(EventSource entity) {
        this.getOne(Wrappers.<EventSource>lambdaQuery()
                .ne(EventSource::getId, entity.getId())
                .eq(EventSource::getName, entity.getName()))
                .ifPresentThrow("名称已存在["+entity.getName()+"]");

        return super.updateById(entity);
    }

    @Override
    public PageResult<IPage<EventSource>> detailPage(Page<EventSource> page, EventSource entity) {
        this.getBaseMapper().detailPage(page, entity);
        return new PageResult<>(page);
    }

    @Override
    public DetailResult<EventSource> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    @Transactional
    public BooleanResult updateConfig(EventSource entity) {
        if(CollectionUtil.isNotEmpty(entity.getModelApiIds())) {
            eventSourceDetailService.remove(Wrappers.<EventSourceDetail>lambdaQuery()
                    .eq(EventSourceDetail::getEventSourceId, entity.getId()));

            List<EventSourceDetail> sourceDetails = entity.getModelApiIds().stream()
                    .map(item -> new EventSourceDetail().setEventSourceId(entity.getId()).setModelApiId(Long.valueOf(item)))
                    .collect(Collectors.toList());

            eventSourceDetailService.saveBatch(sourceDetails);
        } else {
            return BooleanResult.buildFalse("未配置物模型接口");
        }

        return this.updateById(entity);
    }

    @Override
    public ListResult<IVOption> availableSources(Long id) {
        List<IVOption> result = this.getBaseMapper().availableSources(id);
        return new ListResult<>(result);
    }

    @Override
    public BooleanResult removeAllByIds(List<Long> idList) {
        this.listByIds(idList).forEach(item -> {
           if(item.getStatus() == FuncStatus.enabled) {
               throw new ServiceException("请先停用事件源["+item.getName()+"]");
           }
        });

        int result = this.getBaseMapper().removeAllByIds(idList);
        return new BooleanResult(result > 0);
    }

    @Override
    public BooleanResult switchStatus(FuncStatus status, Long id) {

        return BooleanResult.buildTrue("切换成功");
    }

    @Override
    public DetailResult<EventSourceDto> collectDetailById(Long id) {
        EventSourceDto eventSourceDto = this.getBaseMapper().collectDetailById(id);
        return new DetailResult<>(eventSourceDto);
    }

    @Override
    public ListResult<Product> listProductById(Long id, FuncStatus status) {
        return new ListResult<>(this.getBaseMapper().listProductById(id, status));
    }

    @Override
    public ListResult<DeviceGroup> listDeviceGroupById(Long id) {
        List<DeviceGroup> list = this.getBaseMapper().listDeviceGroupById(id);
        return new ListResult<>(list);
    }

    @Override
    public FuncStatusProfileDto countStatusProfile(GroupAndProductParamDto param) {
        return getBaseMapper().countStatusProfile(param);
    }
}
