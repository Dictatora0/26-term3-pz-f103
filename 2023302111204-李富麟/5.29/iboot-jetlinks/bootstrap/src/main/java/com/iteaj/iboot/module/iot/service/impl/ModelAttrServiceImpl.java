package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.dto.StatusSwitchDto;
import com.iteaj.iboot.module.iot.entity.ModelAttr;
import com.iteaj.iboot.module.iot.entity.ModelAttrDict;
import com.iteaj.iboot.module.iot.mapper.ModelAttrDictMapper;
import com.iteaj.iboot.module.iot.mapper.ModelAttrMapper;
import com.iteaj.iboot.module.iot.service.IModelAttrService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * 物模型属性 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Service
public class ModelAttrServiceImpl extends BaseServiceImpl<ModelAttrMapper, ModelAttr> implements IModelAttrService {

    @Autowired
    private ModelAttrDictMapper modelAttrDictMapper;

    @Override
    @Transactional
    public BooleanResult save(ModelAttr entity) {
        this.getOne(Wrappers.<ModelAttr>lambdaQuery()
                .eq(ModelAttr::getField, entity.getField())
                .eq(ModelAttr::getProductId, entity.getProductId()))
                .ifPresentThrow("属性字段已存在["+entity.getField()+"]");
        entity.setCreateTime(new Date());
        resolveField(entity);
        BooleanResult booleanResult = super.save(entity).ifPresent(item -> {
            if(!CollectionUtils.isEmpty(entity.getDicts())) {
                entity.getDicts().forEach(dict -> {
                    dict.setModelAttrId(entity.getId());
                    modelAttrDictMapper.insert(dict);
                });
            }
        });

        return booleanResult;
    }

    @Override
    public BooleanResult updateById(ModelAttr entity) {
        this.getOne(Wrappers.<ModelAttr>lambdaQuery()
                .ne(ModelAttr::getId, entity.getId())
                .eq(ModelAttr::getField, entity.getField())
                .eq(ModelAttr::getProductId, entity.getProductId()))
                .ifPresentThrow("属性字段已存在["+entity.getField()+"]");

        // 字段被使用则不允许修改字段名
        this.getById(entity.getId()).ifNotPresentThrow("属性不存在").ifPresent(item -> {
           if(!Objects.equals(item.getField(), entity.getField())) {
                if(hasBinded(Arrays.asList(item.getId()))) {
                    throw new ServiceException("属性已被绑定字段名不允许修改");
                }
           }
        });
        resolveField(entity);
        return super.updateById(entity);
    }

    private void resolveField(ModelAttr entity) {
        if(entity.getDataType() == null) {
            throw new ServiceException("类型必填");
        }
        entity.setRealType(entity.getDataType());
        if(entity.getDataType().startsWith("[")) {
            entity.setEnumerate(true); // 属于枚举类型
            String realType = entity.getDataType().substring(1, entity.getDataType().length() - 1); // 真实类型
            entity.setRealType(realType);
        }
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isEmpty(idList)) {
            return BooleanResult.buildFalse("未指定要删除的记录");
        }

        if(idList.size() > 1) {
            return BooleanResult.buildFalse("不支持批量删除");
        }

        Serializable id = idList.stream().findFirst().get();
        if(this.getBaseMapper().isEnabledOfProduct(id)) {
            return BooleanResult.buildFalse("请先停用产品");
        }

        if(this.hasBinded(idList)) {
            return BooleanResult.buildFalse("此属性已被绑定");
        }

        this.getBaseMapper().deleteByJoin(id);
        return BooleanResult.buildTrue("删除成功");
    }

    @Override
    public DetailResult<ModelAttr> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public Result<Boolean> saveOrUpdateModelAttrDict(ModelAttrDict entity) {
        if(entity.getId() == null) {
            return new BooleanResult(modelAttrDictMapper.insert(entity) > 0);
        } else {
            return new BooleanResult(modelAttrDictMapper.updateById(entity) > 0);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> removeModelAttrDictByIds(List<Long> idList) {
        if(!CollectionUtils.isEmpty(idList)) {
            modelAttrDictMapper.deleteBatchIds(idList);
        }

        return new BooleanResult(true);
    }

    @Override
    public ListResult<ModelAttrDict> listModelAttrDicts(ModelAttrDict entity) {
        List<ModelAttrDict> modelAttrDicts = this.getBaseMapper().listDetailDict(entity);
        return new ListResult<>(modelAttrDicts);
    }

    @Override
    public boolean hasBinded(Collection<? extends Serializable> idList) {
        return this.getBaseMapper().hasBinded(idList);
    }

    @Override
    public BooleanResult switchCtrlStatus(StatusSwitchDto<Boolean> dto) {
        ModelAttr modelAttr = this.getById(dto.getId()).ifNotPresentThrow("模型属性不存在").getData();
        return new BooleanResult(this.getBaseMapper()
                .switchCtrlStatus(modelAttr.getProductId(), dto.getId(), dto.getStatus()) > 0);
    }
}
