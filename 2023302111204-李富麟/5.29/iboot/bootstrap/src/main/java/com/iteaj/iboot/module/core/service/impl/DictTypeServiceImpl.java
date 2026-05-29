package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.iboot.module.core.entity.DictType;
import com.iteaj.iboot.module.core.mapper.IDictTypeDao;
import com.iteaj.iboot.module.core.service.IDictDataService;
import com.iteaj.iboot.module.core.service.IDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;

/**
 * jdk：1.8
 *
 * @author iteaj
 * create time：2019/7/13
 */
@Service
public class DictTypeServiceImpl extends BaseServiceImpl<IDictTypeDao, DictType> implements IDictTypeService {

    private final IDictDataService dictDataService;

    public DictTypeServiceImpl(IDictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public BooleanResult save(DictType entity) {
        getOne(Wrappers.<DictType>lambdaQuery().eq(DictType::getType, entity.getType())).ofNullable()
                .ifPresent(item -> {throw new ServiceException("字典类型重复["+entity.getType()+"]");});

        getOne(Wrappers.<DictType>lambdaQuery().eq(DictType::getName, entity.getName())).ofNullable()
                .ifPresent(item -> {throw new ServiceException("字典名称重复["+entity.getName()+"]");});

        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(DictType entity) {
        getOne(Wrappers.<DictType>lambdaQuery().eq(DictType::getName, entity.getName())).ofNullable()
                .ifPresent(item -> {
                    if(item.getId().compareTo(entity.getId()) != 0) {
                        throw new ServiceException("字典名称重复["+entity.getName()+"]");
                    }
                });

        return super.updateById(entity);
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isNotEmpty(idList)) {
            list(Wrappers.<DictType>lambdaQuery().in(DictType::getId, idList))
                    .stream().forEach(item -> dictDataService.removeByType(item.getType()));
        }

        return super.removeByIds(idList);
    }
}
