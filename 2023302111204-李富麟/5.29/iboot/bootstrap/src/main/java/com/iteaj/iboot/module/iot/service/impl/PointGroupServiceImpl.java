package com.iteaj.iboot.module.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.entity.GroupPoint;
import com.iteaj.iboot.module.iot.entity.PointGroup;
import com.iteaj.iboot.module.iot.mapper.GroupPointMapper;
import com.iteaj.iboot.module.iot.mapper.PointGroupMapper;
import com.iteaj.iboot.module.iot.service.IPointGroupService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 点位组 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
@Service
public class PointGroupServiceImpl extends BaseServiceImpl<PointGroupMapper, PointGroup> implements IPointGroupService {

    private final GroupPointMapper groupPointMapper;

    public PointGroupServiceImpl(GroupPointMapper groupPointMapper) {
        this.groupPointMapper = groupPointMapper;
    }

    @Override
    public PageResult<IPage<PointGroup>> detailOfPage(Page<PointGroup> page, PointGroup entity) {
        return new PageResult<>(this.getBaseMapper().detailOfPage(page, entity));
    }

    @Override
    public DetailResult<PointGroup> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public Boolean isBindSignals(Collection<? extends Serializable> list) {
        Boolean bindSignals = getBaseMapper().isBindSignals(list);
        return bindSignals == null ? false : bindSignals;
    }

    @Override
    public boolean isBindCollectTask(Collection<? extends Serializable> idList) {
        return getBaseMapper().isBindCollectTask(idList);
    }

    @Override
    @Transactional
    public BooleanResult save(PointGroup entity) {
        if(CollectionUtils.isEmpty(entity.getSignalIds())) {
            return new BooleanResult(false).fail("未选择信号点位");
        } else {
            entity.setSignalNum(entity.getSignalIds().size());
            BooleanResult save = super.save(entity);
            List<GroupPoint> groupPoints = entity.getSignalIds().stream()
                    .map(item -> new GroupPoint(Long.valueOf(item), entity.getId()))
                    .collect(Collectors.toList());

            this.groupPointMapper.batchSaveGroupPoint(groupPoints);

            return save;
        }
    }

    @Override
    @Transactional
    public BooleanResult updateById(PointGroup entity) {
        if(CollectionUtils.isEmpty(entity.getSignalIds())) {
            return new BooleanResult(false).fail("未选择信号点位");
        } else {
            groupPointMapper.delete(Wrappers.<GroupPoint>lambdaQuery()
                    .eq(GroupPoint::getGroupId, entity.getId()));

            List<GroupPoint> groupPoints = entity.getSignalIds().stream()
                    .map(item -> new GroupPoint(Long.valueOf(item), entity.getId()))
                    .collect(Collectors.toList());

            entity.setSignalNum(entity.getSignalIds().size());
            this.groupPointMapper.batchSaveGroupPoint(groupPoints);
        }

        return super.updateById(entity);
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        // 已经绑定了采集任务
        if(this.isBindCollectTask(idList)) {
            return BooleanResult.buildFalse("请先删除对应的采集任务");
        }

        return super.removeByIds(idList).ifPresentAndTrue(status -> {
            // 移除对应的组点位信息
            this.groupPointMapper.delete(Wrappers.<GroupPoint>lambdaQuery().in(GroupPoint::getGroupId, idList));
        });
    }
}
