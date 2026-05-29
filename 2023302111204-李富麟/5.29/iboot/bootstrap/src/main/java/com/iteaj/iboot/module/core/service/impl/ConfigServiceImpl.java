package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.iboot.module.core.mapper.IConfigDao;
import com.iteaj.iboot.module.core.entity.Config;
import com.iteaj.iboot.module.core.service.IConfigService;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import org.springframework.stereotype.Service;

/**
 * create time: 2019/12/4
 *
 * @author iteaj
 * @since 1.0
 */
@Service
public class ConfigServiceImpl extends BaseServiceImpl<IConfigDao, Config> implements IConfigService {

    @Override
    public BooleanResult save(Config entity) {
        getOne(Wrappers.<Config>lambdaQuery().eq(Config::getLabel, entity.getLabel())).ofNullable()
                .ifPresent(item -> {throw new ServiceException("配置标识重复["+entity.getLabel()+"]");});

        getOne(Wrappers.<Config>lambdaQuery().eq(Config::getName, entity.getName())).ofNullable()
                .ifPresent(item -> {throw new ServiceException("配置名称重复["+entity.getName()+"]");});

        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(Config entity) {
        getOne(Wrappers.<Config>lambdaQuery().eq(Config::getName, entity.getName())).ofNullable()
                .ifPresent(item -> {
                    // 名称重复
                    if(entity.getId().compareTo(item.getId()) != 0) {
                        throw new ServiceException("配置名称重复["+entity.getName()+"]");
                    }
                });
        return super.updateById(entity);
    }
}
