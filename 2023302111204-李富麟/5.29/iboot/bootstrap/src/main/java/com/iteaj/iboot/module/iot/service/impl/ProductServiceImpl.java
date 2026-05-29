package com.iteaj.iboot.module.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.ClientProtocolSupplier;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.AttrType;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.framework.spi.iot.listener.EntityPayload;
import com.iteaj.framework.spi.iot.listener.IotEvenPublisher;
import com.iteaj.framework.spi.iot.listener.IotEventType;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import com.iteaj.framework.spi.iot.protocol.ProtocolModel;
import com.iteaj.framework.spi.iot.protocol.ProtocolModelApiConfig;
import com.iteaj.framework.spi.iot.protocol.ProtocolModelAttr;
import com.iteaj.iboot.common.CacheKeys;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.collect.model.DeviceStatusModelApiManager;
import com.iteaj.iboot.module.iot.consts.*;
import com.iteaj.iboot.module.iot.dto.DeviceTypeCountDto;
import com.iteaj.iboot.module.iot.dto.FuncStatusProfileDto;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.dto.ProtocolToProductModel;
import com.iteaj.iboot.module.iot.entity.*;
import com.iteaj.iboot.module.iot.mapper.ProductMapper;
import com.iteaj.iboot.module.iot.service.*;
import com.iteaj.iboot.module.iot.utils.IotNetworkUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备型号 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
@Service
@CacheConfig(cacheNames = {CacheKeys.IBOOT_CACHE_KEY_PRODUCT})
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements IProductService {

    private final CacheManager cacheManager;
    private final IotCacheManager iotCacheManager;
    private final IProtocolService protocolService;
    private final IModelApiService modelApiService;
    private final IModelAttrService modelAttrService;

    public ProductServiceImpl(IProtocolService protocolService
            , IModelApiService modelApiService, IModelAttrService modelAttrService
            , IotCacheManager iotCacheManager, CacheManager cacheManager) {
        this.protocolService = protocolService;
        this.modelApiService = modelApiService;
        this.modelAttrService = modelAttrService;
        this.iotCacheManager = iotCacheManager;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional
    public BooleanResult save(Product entity) {
        this.getOne(Wrappers.<Product>lambdaQuery()
                .eq(Product::getProductTypeId, entity.getProductTypeId())
                .eq(Product::getName, entity.getName()))
                .ifPresent(one -> {throw new ServiceException("此产品已经存在["+entity.getName()+"]");});

        this.getByCode(entity.getCode()).ifPresentThrow("此产品代码已经存在["+entity.getCode()+"]");

        this.protocolService.getById(entity.getProtocolId())
                .ifNotPresentThrow("协议不存在")
                .ifPresent(item -> entity.setProtocolCode(item.getCode()));

        BooleanResult save = super.save(entity);
        // 保存协议对应的指令到物模型
        saveProtocolToModel(entity);

        return save;
    }

    @Override
    @Transactional
    public BooleanResult updateById(Product entity) {
        this.getOne(Wrappers.<Product>lambdaQuery()
                .ne(Product::getId, entity.getId())
                .eq(Product::getProductTypeId, entity.getProductTypeId())
                .eq(Product::getName, entity.getName()))
                .ofNullable().ifPresent(one -> {throw new ServiceException("此产品已经存在["+entity.getName()+"]");});

        this.getByCode(entity.getCode()).ifPresent(item -> {
            if(!item.getId().equals(entity.getId())) {
                throw new ServiceException("此产品代码已经存在["+entity.getCode()+"]");
            }
        });

        this.protocolService.getById(entity.getProtocolId())
                .ifNotPresentThrow("协议不存在")
                .ifPresent(item -> entity.setProtocolCode(item.getCode()));

        // 如果修改了协议需要删除所有物模型数据
        this.getById(entity.getId()).ifPresent(product -> {
            if(!product.getProtocolId().equals(entity.getProtocolId())) {
                // 移除缓存里面的无模型信息
                modelApiService.list(Wrappers.<ModelApi>lambdaQuery().eq(ModelApi::getProductId, product.getId()))
                        .forEach(modelApi -> Objects.requireNonNull(cacheManager.getCache(CacheKeys.IBOOT_CACHE_KEY_MODEL_API)).evict(modelApi.getCode()));

                // 移除所有物模型信息
                this.removeModel(entity.getId());

                // 保存协议对应的指令到物模型
                saveProtocolToModel(entity);
            }
        });

        // 网关子设备的产品需要绑定父网关产品
        if(entity.getDeviceType() != DeviceType.Child) {
            entity.setParentId(null);
        } else if(entity.getParentId() == null){
            throw new ServiceException("网关子设备产品需绑定父产品");
        }

        return super.updateById(entity).ifPresentAndTrue(item -> cacheManager
                .getCache(CacheKeys.IBOOT_CACHE_KEY_PRODUCT).evict(entity.getId()));
    }

    private void saveProtocolToModel(Product entity) {
        ProtocolToProductModel productModel = protocolToProductModel(entity.getProtocolId());
        // 批量保存物模型属性
        if(!CollectionUtils.isEmpty(productModel.getAttrs())) {
            productModel.getAttrs().forEach(item -> item.setProductId(entity.getId()));
            modelAttrService.saveBatch(productModel.getAttrs());
        }

        // 批量保存事件接口
//        List<ModelApi> apis = new ArrayList<>();
//        if(!CollectionUtils.isEmpty(productModel.getEventApis())) {
//            apis.addAll(productModel.getEventApis());
//        }
//
//        if(!CollectionUtils.isEmpty(productModel.getFuncApis())) {
//            productModel.getFuncApis().forEach(item -> item.setProductId(entity.getId()));
//            apis.addAll(productModel.getFuncApis());
//        }
//
//        if(!CollectionUtils.isEmpty(apis)) {
//            apis.forEach(item -> {
//                item.setProductId(entity.getId());
//                modelApiService.save(item);
//                List<ModelApiConfig> apiConfigs = new ArrayList<>();
//
//                if(!CollectionUtils.isEmpty(item.getUpConfig())) {
//                    apiConfigs.addAll(item.getUpConfig());
//                }
//
//                if(!CollectionUtils.isEmpty(item.getDownConfig())) {
//                    apiConfigs.addAll(item.getDownConfig());
//                }
//
//                // 批量保存api配置
//                if(!CollectionUtils.isEmpty(apiConfigs)) {
//                    apiConfigs.forEach(config -> config.setProductId(entity.getId()));
//                    modelApiConfigService.saveBatch(apiConfigs);
//                }
//            });
//        }
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isEmpty(idList)) {
            return new BooleanResult(false);
        }

        if(idList.size() > 1) {
            return new BooleanResult(false).fail("不支持批量删除");
        }

        // 是否已经有绑定设备
        this.hasBind(idList).ifPresentAndTrueThrow("此产品已被设备绑定");

        this.getOne(Wrappers.<Product>lambdaQuery().in(Product::getId, idList).last("limit 1"))
                .ifPresent(item -> {
            if(item.getStatus() == FuncStatus.enabled) {
                throw new ServiceException("请先停用产品");
            }
        });

        return super.removeByIds(idList).ifPresent(item -> idList.forEach(id -> {
            // 先移除缓存
            modelApiService.list(Wrappers.<ModelApi>lambdaQuery().eq(ModelApi::getProductId, id))
                    .forEach(modelApi -> Objects.requireNonNull(cacheManager.getCache(CacheKeys.IBOOT_CACHE_KEY_MODEL_API)).evict(modelApi.getCode()));

            // 移除所有物模型配置
            this.removeModel((Long) id);
        }));
    }

    @Override
    public PageResult<IPage<ProductDto>> pageOfDetail(Page page, Product entity) {
        return new PageResult<>(this.getBaseMapper().pageOfDetail(page, entity));
    }

    @Override
    public void removeModel(Long productId) {
        this.getBaseMapper().removeModel(productId);
    }

    @Override
    public ProtocolToProductModel protocolToProductModel(Long protocolId) {
        ProtocolToProductModel model = new ProtocolToProductModel();
        this.protocolService.getByDetail(protocolId).ifPresent(item -> {
            if(item.getImplMode() != ProtocolImplMode.Custom) {
                DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(item.getCode());
                if(supplier != null) {
                    ProtocolModel protocol = supplier.getProtocol();

                    // 协议接口
                    protocol.getApis().values().stream().forEach(api -> {
                        // 协议接口
                        ModelApi modelApi = new ModelApi();
                        BeanUtils.copyProperties(api, modelApi);

                        // 协议接口上行配置
                        List<ModelApiConfig> upConfig = api.getUpConfig()
                                .values().stream().map(config -> {
                                    ProtocolModelAttr attr = protocol.getAttrs().get(config.getProtocolModelAttrField());
                                    // 将协议模型的api配置转换成协议api配置
                                    return convertProtocolApiConfig(modelApi, config, attr);
                                }).collect(Collectors.toList());

                        // 协议接口下行配置
                        List<ModelApiConfig> downConfig = api.getDownConfig()
                                .values().stream().map(config -> {
                                    ProtocolModelAttr attr = protocol.getAttrs().get(config.getProtocolModelAttrField());
                                    // 将协议模型的api配置转换成协议api配置
                                    return convertProtocolApiConfig(modelApi, config, attr);
                                }).collect(Collectors.toList());

                        modelApi.setProtocol(true); //协议指令为null
                        if(api.getType() == ProtocolApiType.event) {
                            model.getEventApis().add(modelApi.setUpConfig(upConfig).setDownConfig(downConfig));
                        } else if(api.getType() == ProtocolApiType.func) {
                            model.getFuncApis().add(modelApi.setUpConfig(upConfig).setDownConfig(downConfig));
                        }
                    });

                    // 协议属性转模型属性
                    List<ModelAttr> attrs = protocol.getAttrs().values().stream()
                            .filter(attr -> attr.getAttrType() == AttrType.R || attr.getAttrType() == AttrType.RW)
                        .map(attr -> new ModelAttr()
                                .setName(attr.getName())
                                .setField(attr.getField())
                                .setRemark(attr.getRemark())
                                .setDataType(attr.getDataType().getValue())
                                .setRealType(attr.getDataType().getValue())
                                .setAttrType(attr.getAttrType())
                                .setEnumerate(false)
                                .setOrigin(AttrOrigin.Protocol))
                        .collect(Collectors.toList());
                    model.setAttrs(attrs);
                }
            } else {
                List<ModelAttr> attrList = item.getAttrs().stream().map(attr -> {
                    ModelAttr modelAttr = new ModelAttr();
                    BeanUtils.copyProperties(attr, modelAttr);
                    return modelAttr.setOrigin(AttrOrigin.Protocol);
                }).collect(Collectors.toList());

                List<ModelApi> funcApis = item.getFuncApis().stream()
                        .map(api -> convertProtocolApiToModelApi(api))
                        .collect(Collectors.toList());
                List<ModelApi> eventApis = item.getEventApis().stream()
                        .map(api -> convertProtocolApiToModelApi(api))
                        .collect(Collectors.toList());

                model.setFuncApis(funcApis).setEventApis(eventApis).setAttrs(attrList);
            }
        });

        return model;
    }

    @Override
    public DetailResult<Product> getByCode(String code) {
        return this.getOne(Wrappers.<Product>lambdaQuery().eq(Product::getCode, code));
    }

    @Override
    public DetailResult<ProductDto> detailById(Long id) {
        Cache cache = cacheManager.getCache(CacheKeys.IBOOT_CACHE_KEY_PRODUCT);
        Cache.ValueWrapper valueWrapper = cache.get(id);
        if(valueWrapper != null) {
            return new DetailResult<>((ProductDto) valueWrapper.get());
        }

        ProductDto productDto = getBaseMapper().detailById(id);
        if(productDto == null) {
            cache.put(id, productDto);
        }

        return new DetailResult<>(productDto);
    }

    @Override
    public BooleanResult hasBind(Collection<? extends Serializable> idList) {
        return new BooleanResult(this.getBaseMapper().hasBind(idList));
    }

    @Override
    public ListResult<ProductDto> listOfDetail(Product entity) {
        return new ListResult<>(this.getBaseMapper().listOfDetail(entity));
    }

    @Override
    public DetailResult<ProductDto> joinDetailById(Long productId) {
        ProductDto productDto = getBaseMapper().joinDetailById(productId);
        return new DetailResult<>(productDto);
    }

    @Override
    public DetailResult<ProductDto> debugById(Long productId) {
        ProductDto productDto = getBaseMapper().joinDetailById(productId);
        if(!CollectionUtils.isEmpty(productDto.getFuncApis())) {
            List<ModelApi> funcApis = productDto.getFuncApis().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getDebug()))
                    .collect(Collectors.toList());

            productDto.setFuncApis(funcApis);
        }

        if(!CollectionUtils.isEmpty(productDto.getEventApis())) {
            List<ModelApi> eventApis = productDto.getEventApis().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getDebug()))
                    .collect(Collectors.toList());

            productDto.setEventApis(eventApis);
        }

        return new DetailResult<>(productDto);
    }

    @Override
    public ListResult<Product> listByPoint() {
        List<Product> list = this.getBaseMapper().listByPoint();
        return new ListResult<>(list);
    }

    @Override
    public ListResult<ProductDto> listJoinDetailByIds(List<Long> productIds) {
        List<ProductDto> list = this.getBaseMapper().listJoinDetailByIds(productIds);
        return new ListResult<>(list);
    }

    @Override
    public boolean hasBindEnabledEventSource(Long id) {
        return this.getBaseMapper().hasBindEnabledEventSource(id);
    }

    @Override
    @Transactional
    public void switchStatus(Long id, FuncStatus status) {
        this.detailById(id).ifNotPresentThrow("查找不到此产品")
            .ifPresent(item -> {
                // 被启用的事件源绑定后产品不能被禁用
                if(status == FuncStatus.disabled) {
                    if(this.hasBindEnabledEventSource(id)) {
                        throw new ServiceException("此产品正被事件源使用中, 请先禁用对应事件源");
                    }
                }

                // 网关子设备的父产品是边缘网关产品
                if(item.getDeviceType() == DeviceType.Child) {
                    if(status == FuncStatus.enabled) {
                        this.getById(item.getParentId()).ifNotPresentThrow("查找不到父产品")
                            .ifPresent(parent -> {
//                                if(parent.getStatus() == FuncStatus.disabled) {
//                                    throw new ServiceException("请先启用父产品["+parent.getName()+"]");
//                                }
                            });
                    }
                } else if(item.getDeviceType() == DeviceType.Gateway) { // 启用边缘网关下面所有的设备
                    DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(item.getProtocolCode());
                    if(supplier == null) {
                        throw new ServiceException("查找不到对应的协议提供[code="+item.getProtocolCode()+"]");
                    }

                    // 校验此网关产品下面有没有已启用的子产品
                    if(status == FuncStatus.disabled) {
//                        this.getOne(Wrappers.<Product>lambdaQuery()
//                                .eq(Product::getParentId, item.getId())
//                                .eq(Product::getStatus, FuncStatus.enabled)
//                                .last("limit 1")).ifPresent(child -> {
//                            throw new ServiceException("请先停用子产品["+child.getName()+"]");
//                        });
                    }

                    if(supplier instanceof ClientProtocolSupplier) {
                        // 先校验产品绑定的网关是否启用
                        if(status == FuncStatus.enabled) {
                            if(item.getGatewayStatus() == GatewayStatus.stop) {
                                throw new ServiceException("请先启用网关["+item.getGatewayName()+"]");
                            }
                        }

                        // 获取此网关产品下面的所有网关设备
                        this.getBaseMapper().listDeviceByProductId(item.getId()).forEach(device -> {
                            try {
                                IotNetworkUtil.networkCtrl(supplier, device, status);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }

                this.update(Wrappers.<Product>lambdaUpdate().eq(Product::getId, item.getId()).set(Product::getStatus, status));
                if(item.getDeviceType() == DeviceType.Child) {
                    if(status == FuncStatus.enabled) {
                        // 启用的时候加入到状态定时任务
                        this.modelApiService.getAsStatusModelApi(id).ifPresent(modelApi -> {
                            DeviceStatusModelApiManager.getInstance().register(modelApi);
                        });
                    } else {
                        // 禁用的时候移除掉此产品的状态接口
                        DeviceStatusModelApiManager.getInstance().remove(id);

                         // 产品禁用关闭所有此产品下的设备
                        iotCacheManager.listByProductCode(item.getCode()).forEach(device -> {
                            try {
                                if(device.getStatus() == DeviceStatus.online) {
                                    device.setStatus(DeviceStatus.offline);
                                    IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    }
                }

                /**
                 * 发布产品状态切换事件
                 */
                IotEvenPublisher.publish(IotEventType.ProductSwitch, new EntityPayload(item.setStatus(status)));
            });
    }

    @Override
    public DetailResult<ProductDto> getCtrlStatusModelApi(Long productId) {
        ProductDto productDto = this.getBaseMapper().getCtrlStatusModelApi(productId);
        return new DetailResult<>(productDto);
    }

    @Override
    public List<ProductDto> listCtrlStatusModelAttrs(List<Long> productIds) {
        return this.getBaseMapper().listCtrlStatusModelAttrs(productIds);
    }

    @Override
    public ListResult<ProductDto> listByProtocolCode(String protocolCode, String productCode) {
        return new ListResult<>(this.getBaseMapper().listByProtocolCode(protocolCode, productCode));
    }

    @Override
    public FuncStatusProfileDto countStatusProfile() {
        return getBaseMapper().countStatusProfile();
    }

    @Override
    public List<NameValueDto> countOfDevice() {
        return getBaseMapper().countOfDevice();
    }

    @Override
    public List<DeviceTypeCountDto> countOfDeviceType() {
        return getBaseMapper().countOfDeviceType();
    }

    @Override
    public ListResult<Product> listOfProtocolCodes(DeviceType deviceType, String[] protocolCodes, TransportProtocol protocol) {
        return new ListResult<>(getBaseMapper().listOfProtocolCodes(deviceType, protocolCodes, protocol));
    }

    private ModelApi convertProtocolApiToModelApi(ProtocolApi api) {
        ModelApi modelApi = new ModelApi();
        BeanUtils.copyProperties(api, modelApi);

        // 协议的功能转成协议指令
        modelApi.setProtocol(true);

        List<ModelApiConfig> upConfigs = api.getUpConfigs().stream().map(config -> {
            ModelApiConfig apiConfig = new ModelApiConfig();
            BeanUtils.copyProperties(config, apiConfig);
            return apiConfig;
        }).collect(Collectors.toList());

        List<ModelApiConfig> downConfigs = api.getDownConfigs().stream().map(config -> {
            ModelApiConfig apiConfig = new ModelApiConfig();
            BeanUtils.copyProperties(config, apiConfig);
            return apiConfig;
        }).collect(Collectors.toList());

        return modelApi.setUpConfig(upConfigs).setDownConfig(downConfigs);
    }

    private ModelApiConfig convertProtocolApiConfig(ModelApi protocolApi, ProtocolModelApiConfig config, ProtocolModelAttr attr) {
        return new ModelApiConfig()
                .setSort(config.getSort())
                .setRemark(config.getRemark())
                .setAttrField(attr.getField())
                .setApiCode(protocolApi.getCode())
                .setFieldType(config.getFieldType())
                .setDirection(config.getDirection());
    }
}
