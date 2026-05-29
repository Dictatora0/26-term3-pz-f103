package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.mapper.CollectDetailMapper;
import com.iteaj.iboot.module.iot.service.ICollectDetailService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class CollectDetailServiceImpl extends BaseServiceImpl<CollectDetailMapper, CollectDetail> implements ICollectDetailService {

    @Override
    public BooleanResult save(CollectDetail entity) {
        this.getOne(Wrappers.<CollectDetail>lambdaQuery()
                .eq(CollectDetail::getCollectTaskId, entity.getCollectTaskId())
                .eq(CollectDetail::getPointGroupId, entity.getPointGroupId()))
                .ifPresentThrow("此采集任务已经包含点位组");

        if(this.hasTaskRunning(entity.getCollectTaskId())) {
            throw new ServiceException("任务运行中, 请先关闭采集任务");
        }

        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(CollectDetail entity) {
        this.getOne(Wrappers.<CollectDetail>lambdaQuery()
                .ne(CollectDetail::getId, entity.getId())
                .eq(CollectDetail::getCollectTaskId, entity.getCollectTaskId())
                .eq(CollectDetail::getPointGroupId, entity.getPointGroupId()))
                .ifPresentThrow("此采集任务已经包含点位组");

        if(this.hasTaskRunning(entity.getCollectTaskId())) {
            throw new ServiceException("任务运行中, 请先关闭采集任务");
        }

        return super.updateById(entity);
    }

    @Override
    public DetailResult<CollectDetail> detailById(Long id) {
        return new DetailResult<>(this.baseMapper.detailById(id));
    }

    @Override
    public boolean hasTaskRunning(Long collectTaskId) {
        return getBaseMapper().hasTaskRunning(collectTaskId);
    }

    @Override
    public PageResult<Page<CollectDetail>> detailPage(Page page, CollectDetail entity) {
        Page<CollectDetail> detailPage = this.baseMapper.detailPage(page, entity);
        return new PageResult<>(detailPage);
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isNotEmpty(idList)) {
            if(idList.size() != 1) {
                throw new ServiceException("不支持批量删除");
            }

            idList.forEach(id -> {
                this.getById(id).ifPresent(detail -> {
                    if(this.hasTaskRunning(detail.getCollectTaskId())) {
                        throw new ServiceException("任务运行中, 请先关闭采集任务");
                    }
                });
            });

            return super.removeByIds(idList);
        } else {
            return BooleanResult.buildFalse("未指定要删除的记录");
        }
    }
}
