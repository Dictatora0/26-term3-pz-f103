package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.*;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.collect.CollectSignal;
import com.iteaj.iboot.module.iot.dto.CollectTaskDto;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.entity.CollectTask;
import com.iteaj.iboot.module.iot.mapper.CollectTaskMapper;
import com.iteaj.iboot.module.iot.service.ICollectTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 数据采集任务 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-08-28
 */
@Service
public class CollectTaskServiceImpl extends BaseServiceImpl<CollectTaskMapper, CollectTask> implements ICollectTaskService {

    @Override
    public Result<IPage<CollectTaskDto>> detailOfPage(Page<CollectTaskDto> page, CollectTaskDto entity) {
        return new PageResult<>(this.getBaseMapper().detailOfPage(page, entity));
    }

    @Override
    public DetailResult<CollectTaskDto> detailById(Long id) {
        return new DetailResult<>(getBaseMapper().detailById(id));
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isNotEmpty(idList)) {
            this.listByIds(idList).forEach(item -> {
                if("run".equals(item.getStatus())) {
                    throw new ServiceException("任务运行中, 请先关闭任务");
                }
            });

            this.getBaseMapper().removeTaskAndDetail(idList);
            return BooleanResult.buildTrue("删除成功");
        }

        return BooleanResult.buildFalse("未指定删除记录");
    }

    @Override
    public CollectTaskDto collectDetailById(Long id) {
        CollectTaskDto collectTaskDto = getBaseMapper().collectDetailById(id);
        resolveCollectTaskDetail(Arrays.asList(collectTaskDto));
        return collectTaskDto;
    }

    @Override
    public ListResult<CollectTaskDto> runningCollectTaskDetail() {
        List<CollectTaskDto> list = this.getBaseMapper().listRunningCollectTaskDetail();
        resolveCollectTaskDetail(list);
        return new ListResult<>(list);
    }

    private void resolveCollectTaskDetail(List<CollectTaskDto> list) {
        if(CollectionUtil.isNotEmpty(list)) {
            // 获取点位组下面的所有设备
            List<Long> groupIds = list.stream().flatMap(item -> CollectionUtil.isNotEmpty(item.getDetails()) ?
                    item.getDetails().stream().map(detail -> detail.getPointGroupId()) : Stream.empty())
                    .distinct().collect(Collectors.toList());

            if(CollectionUtil.isNotEmpty(groupIds)) {
                Map<Long, List<CollectDevice>> deviceByGroupIds = this.listCollectDeviceByGroupIds(groupIds);
                list.stream().forEach(item -> {
                    item.getDetails().forEach(detail -> {
                        List<CollectDevice> collectDevices = deviceByGroupIds.get(detail.getPointGroupId());
                        detail.setDevices(collectDevices);
                    });
                });
            }
        }
    }

    @Override
    public PageResult<Page<CollectDetail>> collectDetailPageById(Page page, Long id) {
        Page<CollectDetail> detailPage = this.baseMapper.collectDetailPageById(page, id);
        return new PageResult<>(detailPage);
    }

    @Override
    public Map<Long, List<CollectDevice>> listCollectDeviceByGroupIds(List<Long> groupIds) {
        if(CollectionUtil.isNotEmpty(groupIds)) {
            List<CollectDevice> collectDevices = this.getBaseMapper().listCollectDeviceByGroupIds(groupIds);
            if(CollectionUtil.isNotEmpty(collectDevices)) {
                Map<Long, List<CollectDevice>> result = new HashMap<>();
                collectDevices.forEach(item -> {
                    List<CollectDevice> devices = result.get(item.getPointGroupId());
                    if(devices == null) {
                        result.put(item.getPointGroupId(), devices = new ArrayList<>());
                    }

                    devices.add(item);
                });

                List<CollectSignal> signals = this.getBaseMapper().listCollectSignalByGroupIds(groupIds);
                if(CollectionUtil.isNotEmpty(signals)) {
                    Map<String, List<CollectSignal>> deviceMaps = new HashMap<>();
                    signals.forEach(signal -> {
                        String key = signal.getPointGroupId() + ":" + signal.getProductId();
                        List<CollectSignal> devices = deviceMaps.get(key);
                        if(devices == null) {
                            deviceMaps.put(key, devices = new ArrayList<>());
                        }

                        devices.add(signal);
                    });

                    collectDevices.forEach(device -> {
                        String key = device.getPointGroupId() + ":" + device.getProductId();
                        List<CollectSignal> collectSignals = deviceMaps.get(key);
                        device.setSignals(collectSignals);
                    });
                }

                return result;
            }
        }

        return Collections.EMPTY_MAP;
    }
}
