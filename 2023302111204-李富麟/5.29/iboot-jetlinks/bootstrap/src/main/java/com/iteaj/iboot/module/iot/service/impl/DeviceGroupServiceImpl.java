package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.*;
import com.iteaj.framework.utils.TreeUtils;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.ProductType;
import com.iteaj.iboot.module.iot.mapper.DeviceGroupMapper;
import com.iteaj.iboot.module.iot.service.IDeviceGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备分组 服务实现类
 * </p>
 *
 * @author iteaj
 */
@Service
public class DeviceGroupServiceImpl extends BaseServiceImpl<DeviceGroupMapper, DeviceGroup> implements IDeviceGroupService {

    @Override
    @Transactional
    public BooleanResult save(DeviceGroup entity) {
        this.getOne(Wrappers.<DeviceGroup>lambdaQuery().eq(DeviceGroup::getName, entity.getName()))
                .ifPresentThrow("分组名称已存在["+entity.getName()+"]");

        DetailResult<DeviceGroup> detailResult = this.getById(entity.getPid());
        super.save(entity.setCreateTime(new Date()));
        if(entity.getPid() != null && entity.getPid() != 0) {
            DeviceGroup parent = detailResult
                    .ifNotPresentThrow("父类型不存在")
                    .getData();
            entity.setPath(parent.getPath()+","+entity.getId());
        } else {
            entity.setPath(entity.getId().toString());
        }

        // 没有设置产品则继承父级的产品
        if(entity.getProductIds() == null || entity.getProductIds().isEmpty()) {
            detailResult.ifPresent(item -> entity.setGroupProductId(item.getGroupProductId()))
                    .ifNotPresent(item -> entity.setGroupProductId(entity.getId()));
        } else {
            entity.setGroupProductId(entity.getId());
        }

        return this.update(Wrappers.<DeviceGroup>lambdaUpdate()
                .set(DeviceGroup::getPath, entity.getPath())
                .set(DeviceGroup::getGroupProductId, entity.getGroupProductId())
                .eq(DeviceGroup::getId, entity.getId()));
    }

    @Override
    public BooleanResult updateById(DeviceGroup entity) {
        this.getOne(Wrappers.<DeviceGroup>lambdaQuery()
                .eq(DeviceGroup::getName, entity.getName())
                .ne(DeviceGroup::getId, entity.getId()))
                .ifPresentThrow("分组名称已存在["+entity.getName()+"]");

        DetailResult<DeviceGroup> parentResult = this.getById(entity.getPid());
        if(entity.getPid() != null && entity.getPid() != 0) {
            DeviceGroup parent = parentResult
                    .ifNotPresentThrow("父分组不存在")
                    .getData();

            entity.setPath(parent.getPath()+","+entity.getId());
        } else {
            entity.setPath(entity.getId().toString());
        }

        BooleanResult booleanResult = super.updateById(entity);

        // 没有设置产品则继承父级的产品
        Long oldGroupProductId = entity.getGroupProductId();
        if(entity.getProductIds() == null || entity.getProductIds().isEmpty()) {
            parentResult.ifPresent(item -> entity.setGroupProductId(item.getGroupProductId()))
                    .ifNotPresent(item -> entity.setGroupProductId(entity.getId()));
        } else {
            entity.setGroupProductId(entity.getId());
        }

        getBaseMapper().updateGroupProductId(entity.getId(), entity.getGroupProductId(), oldGroupProductId);
        return booleanResult;
    }

    @Override
    public ListResult<DeviceGroup> tree(DeviceGroup entity) {
        List<DeviceGroup> groups = this.getBaseMapper().listDetail(entity);
        if(CollectionUtil.isNotEmpty(groups)) {
            List<DeviceGroup> deviceGroups = TreeUtils.toTrees(groups)
                    .stream().collect(Collectors.toList());

            return new ListResult<>(deviceGroups);
        }

        return new ListResult<>(Collections.EMPTY_LIST);
    }

    @Override
    public boolean hasBindDevice(Serializable id) {
        return getBaseMapper().hasBindDevice(id);
    }

    @Override
    public DetailResult<DeviceGroup> detailById(Long id) {
        return new DetailResult<>(getBaseMapper().detailById(id));
    }

    @Override
    public DetailResult<DeviceGroup> getGroupProductById(Long id) {
        return new DetailResult<>(getBaseMapper().getGroupProductById(id));
    }

    @Override
    public boolean hasBindEventSource(Serializable id) {
        return this.getBaseMapper().hasBindEventSource(id);
    }

    @Override
    public PageResult<IPage<DeviceGroup>> detailPage(Page<DeviceGroup> page, DeviceGroup entity) {
        this.getBaseMapper().detailPage(page, entity);
        return new PageResult<>(page);
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isEmpty(idList)) {
            return BooleanResult.buildFalse("未指定要删除的记录");
        }
        if(idList.size() > 1) {
            return BooleanResult.buildFalse("不支持批量删除");
        }

        Serializable id = idList.stream().findFirst().get();
        this.count(Wrappers.<DeviceGroup>lambdaQuery().eq(DeviceGroup::getPid, id))
                .ifPresent(count -> {
            if(count > 0) {
                throw new ServiceException("请先删除子分组");
            }
        });

        // 校验是否已绑定设备
        if(this.hasBindDevice(id)) {
            throw new ServiceException("请先解绑此分组下的设备");
        }

        // 校验是否已被事件源绑定
        if(this.hasBindEventSource(id)) {
            throw new ServiceException("请先解绑此分组下的事件源");
        }

        return super.removeByIds(idList);
    }
}
