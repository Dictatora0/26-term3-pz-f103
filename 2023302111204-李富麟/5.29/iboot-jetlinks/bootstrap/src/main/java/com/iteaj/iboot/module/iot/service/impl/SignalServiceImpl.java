package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.*;
import com.iteaj.iboot.module.iot.entity.Signal;
import com.iteaj.iboot.module.iot.mapper.SignalMapper;
import com.iteaj.iboot.module.iot.service.IPointGroupService;
import com.iteaj.iboot.module.iot.service.ISignalService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 寄存器点位 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
@Service
public class SignalServiceImpl extends BaseServiceImpl<SignalMapper, Signal> implements ISignalService {

    @Autowired
    private IPointGroupService pointGroupService;

    @Override
    public BooleanResult save(Signal entity) {
        this.getOne(Wrappers.<Signal>lambdaQuery()
                .eq(Signal::getProductId, entity.getProductId())
                .eq(Signal::getFieldName, entity.getFieldName()))
                .ifPresentThrow("当前产品已经包含字段["+entity.getFieldName()+"]");
        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(Signal entity) {
        this.getOne(Wrappers.<Signal>lambdaQuery()
                .ne(Signal::getId, entity.getId())
                .eq(Signal::getProductId, entity.getProductId())
                .eq(Signal::getFieldName, entity.getFieldName()))
                .ifPresentThrow("当前产品已经包含字段["+entity.getFieldName()+"]");
        return super.updateById(entity);
    }

    @Override
    public Result<IPage<Signal>> detailByPage(Page<Signal> page, Signal entity) {
        return new PageResult<>(getBaseMapper().detailByPage(page, entity));
    }

    @Override
    public ListResult<Signal> listByProductIds(List<Long> productIds, String name) {
        if(CollectionUtils.isEmpty(productIds) && StrUtil.isBlank(name)) {
            return new ListResult<>(Collections.EMPTY_LIST);
        }

        return new ListResult<>(this.getBaseMapper().listByProductIds(productIds, name));
    }

    @Override
    public DetailResult<Signal> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isEmpty(idList)) {
            return new BooleanResult(false);
        }

        if(pointGroupService.isBindSignals(idList)) {
            throw new ServiceException("此点位已被点位组绑定");
        }

        return super.removeByIds(idList);
    }
}
