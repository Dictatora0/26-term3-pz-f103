package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.iboot.common.CacheKeys;
import com.iteaj.iboot.module.core.entity.DictData;
import com.iteaj.iboot.module.core.mapper.IDictDataDao;
import com.iteaj.iboot.module.core.service.IDictDataService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * jdk：1.8
 *
 * @author iteaj
 * create time：2019/6/24
 */
@Service
@CacheConfig(cacheNames = CacheKeys.IBOOT_CACHE_DICT)
public class DictDataServiceImpl extends BaseServiceImpl<IDictDataDao, DictData> implements IDictDataService {

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult save(DictData entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult updateById(DictData entity) {
        return super.updateById(entity);
    }

    @Override
    @Cacheable(key = "'selectByType:'+#type")
    public ListResult<DictData> selectByType(String type) {
        List<DictData> list = getBaseMapper().selectList(Wrappers.<DictData>query().eq("type", type));
        return new ListResult<>(list);
    }

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        return super.removeByIds(idList);
    }

    @Override
    @CacheEvict(key = "'selectByType'+#type", beforeInvocation = true)
    public BooleanResult removeByType(String type) {
        return this.remove(Wrappers.<DictData>lambdaQuery().eq(DictData::getType, type));
    }

    @Override
    @Cacheable(key = "'list'")
    public ListResult<DictData> list() {
        return super.list();
    }

}
