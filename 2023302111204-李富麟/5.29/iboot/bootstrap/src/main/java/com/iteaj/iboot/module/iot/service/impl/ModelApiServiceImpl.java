package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.consts.ApiConfigDirection;
import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.consts.PointProtocolConfig;
import com.iteaj.iboot.common.CacheKeys;
import com.iteaj.iboot.module.iot.collect.model.DeviceStatusModelApiManager;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelApiConfig;
import com.iteaj.iboot.module.iot.mapper.ModelApiMapper;
import com.iteaj.iboot.module.iot.service.IModelApiConfigService;
import com.iteaj.iboot.module.iot.service.IModelApiService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 物模型接口 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Service
@CacheConfig(cacheNames = {CacheKeys.IBOOT_CACHE_KEY_MODEL_API})
public class ModelApiServiceImpl extends BaseServiceImpl<ModelApiMapper, ModelApi> implements IModelApiService {

    private final CacheManager cacheManager;
    private final IModelApiConfigService modelApiConfigService;

    public ModelApiServiceImpl(CacheManager cacheManager, IModelApiConfigService modelApiConfigService) {
        this.cacheManager = cacheManager;
        this.modelApiConfigService = modelApiConfigService;
    }

    @Override
    @Transactional
    public BooleanResult save(ModelApi entity) {
        entity.setCreateTime(new Date());
        this.getByCode(entity.getCode()).ifPresentThrow("接口码已存在["+entity.getCode()+"]");

        if(!CollectionUtils.isEmpty(entity.getDownConfig())) {
            entity.getDownConfig().forEach(item -> resolverModelApiConfig(entity, item));
            this.modelApiConfigService.saveBatch(entity.getDownConfig());
        }

        if(!CollectionUtils.isEmpty(entity.getUpConfig())) {
            entity.getUpConfig().forEach(item -> resolverModelApiConfig(entity, item));
            this.modelApiConfigService.saveBatch(entity.getUpConfig());
        }

        return super.save(entity);
    }

    private void resolverModelApiConfig(ModelApi entity, ModelApiConfig config) {
        config.setApiCode(entity.getCode());
        if(StrUtil.isBlank(config.getDataType())) {
            throw new ServiceException("属性类型[attrType]不能为空");
        }

        if(StrUtil.isBlank(config.getProtocolAttrField())) {
            throw new ServiceException("协议属性[protocolAttrField]不能为空");
        }

        // 如果是点位协议, 同一款产品下面上/下行的点位地址必须唯一
        if(config.getProtocolAttrField().equals(PointProtocolConfig.POINT_ADDRESS)) {
            CtrlMode ctrlMode = this.getProtocolCtrlModelByProductId(entity.getProductId());
            if (ctrlMode == CtrlMode.POINT) {
                if(config.getValue().startsWith("@")) { //
                    throw new ServiceException("点位地址错误["+config.getValue()+"], 不允许提及模型属性");
                }

                // todo modbus协议不同寄存器的地址可以相同
//                if(this.getBaseMapper().hasSameAddress(config.getId(), entity.getProductId(), entity.getType(), PointProtocolConfig.POINT_ADDRESS, config.getValue())) {
//                    throw new ServiceException("同一个产品不允许有相同的点位地址,"+config.getDirection().getDesc()+"点位地址["+config.getValue()+"]已经存在");
//                }
            }
        }
        if(StrUtil.isNotBlank(config.getValue())) {
            // 下行配置使用@提及模型属性
            if(config.getDirection() == ApiConfigDirection.DOWN) {
                if(config.getValue().startsWith("@")) {
                    if(config.getModelAttrId() == null || StrUtil.isBlank(config.getAttrField())) {
                        throw new ServiceException("未指定模型属性[modelAttrId或attrField]");
                    }
                } else {
                    config.setModelAttrId(null).setAttrField(null).setAttrName(null);
                }
            }
        }
    }

    @Override
    @Transactional
    public BooleanResult updateById(ModelApi entity) {
        if(this.getBaseMapper().isEnabled(entity.getProductId())) {
            throw new ServiceException("请先停用产品后更新");
        }

        this.getByCode(entity.getCode()).ifPresent(item -> {
           if(!item.getId().equals(entity.getId())) {
               throw new ServiceException("接口码已存在["+entity.getCode()+"]");
           }
        });

        if(!CollectionUtils.isEmpty(entity.getDownConfig())) {
            entity.getDownConfig().forEach(item -> resolverModelApiConfig(entity, item));
            // 重新添加配置
            this.modelApiConfigService.updateBatchById(entity.getDownConfig());
        }

        if(!CollectionUtils.isEmpty(entity.getUpConfig())) {
            entity.getUpConfig().forEach(item -> resolverModelApiConfig(entity, item));
            this.modelApiConfigService.updateBatchById(entity.getUpConfig());
        }

        return super.updateById(entity).ifPresentAndTrue(item -> cacheManager
                .getCache(CacheKeys.IBOOT_CACHE_KEY_MODEL_API).evict(entity.getCode()));
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        listByIds(idList).stream().filter(item -> item != null).forEach(item -> {
            if(this.getBaseMapper().isEnabled(item.getProductId())) {
                throw new ServiceException("请先停用产品后删除");
            }

            modelApiConfigService.remove(Wrappers.<ModelApiConfig>lambdaQuery()
                    .eq(ModelApiConfig::getApiCode, item.getCode())
                    .eq(ModelApiConfig::getProductId, item.getProductId()));

            cacheManager.getCache(CacheKeys.IBOOT_CACHE_KEY_MODEL_API).evict(item.getCode());
        });
        return super.removeByIds(idList);
    }

    @Override
    public ListResult<ModelApi> detailsOfProductId(Long productId) {
        List<ModelApi> list = this.getBaseMapper().detailsOfProductId(productId);
        return new ListResult<>(list);
    }

    @Override
    public DetailResult<ModelApi> getByProductIdAndCode(Long productId, String code) {
        return this.getOne(Wrappers.<ModelApi>lambdaQuery()
                .eq(ModelApi::getProductId, productId)
                .eq(ModelApi::getCode, code));
    }

    @Override
    public DetailResult<ModelApi> getByCode(String code) {
        return this.getOne(Wrappers.<ModelApi>lambdaQuery().eq(ModelApi::getCode, code));
    }

    @Override
    public Result<ModelApi> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public BooleanResult updateAsStatus(Long productId, String code) {
        if(productId == null || StrUtil.isBlank(code)) {
            return BooleanResult.buildFalse("参数错误[productId="+productId+", code="+code+"]");
        }

        this.getByCode(code).ifNotPresentThrow("协议不存在["+code+"]").ifPresent(api -> {
            if(api.getFuncType() == FuncType.R) { // 采集设备状态
                this.getBaseMapper().updateAsStatus(productId, code, api.getFuncType());
                this.getAsStatusModelApi(productId).ifPresent(modelApi -> {
                    if(this.getBaseMapper().isEnabledOfProduct(productId)) { // 如果产品启用则放到设备状态采集管理里面
                        DeviceStatusModelApiManager.getInstance().update(modelApi);
                    }
                });
            } else {
                // 如果包含有控制状态的属性
                if(this.getBaseMapper().hasCtrlStatus(productId, code)) {
                    this.getBaseMapper().updateAsStatus(productId, code, api.getFuncType());
                } else {
                    throw new ServiceException("没有包含控制属性");
                }
            }
        });
        return BooleanResult.buildTrue("设置状态成功");
    }

    @Override
    public CtrlMode getProtocolCtrlModelByProductId(Long productId) {
        return this.getBaseMapper().getProtocolCtrlModelByProductId(productId);
    }

    @Override
    public ListResult<ModelApi> listAsStatusModelApi() {
        return new ListResult<>(this.getBaseMapper().listAsStatusModelApi());
    }

    @Override
    public DetailResult<ModelApi> getAsStatusModelApi(Long productId) {
        return new DetailResult<>(this.getBaseMapper().getAsStatusModelApi(productId));
    }

    @Override
    public DetailResult<ModelApi> detailByCode(String modelApiCode) {
        Cache cache = cacheManager.getCache(CacheKeys.IBOOT_CACHE_KEY_MODEL_API);
        Cache.ValueWrapper valueWrapper = cache.get(modelApiCode);
        if(valueWrapper != null) {
            return new DetailResult<>((ModelApi) valueWrapper.get());
        }

        ModelApi modelApi = this.getBaseMapper().detailByCode(modelApiCode);
        if(modelApi != null) {
            cache.put(modelApiCode, modelApi);
        }

        return new DetailResult<>(modelApi);
    }
}
