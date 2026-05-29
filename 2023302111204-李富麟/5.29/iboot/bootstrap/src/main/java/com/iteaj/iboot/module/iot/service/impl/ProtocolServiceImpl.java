package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.framework.spi.iot.protocol.*;
import com.iteaj.iboot.module.iot.dto.ProtocolDto;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.iteaj.iboot.module.iot.entity.ProtocolApi;
import com.iteaj.iboot.module.iot.entity.ProtocolApiConfig;
import com.iteaj.iboot.module.iot.entity.ProtocolAttr;
import com.iteaj.iboot.module.iot.mapper.ProtocolMapper;
import com.iteaj.iboot.module.iot.service.IProtocolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 报文协议 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Service
public class ProtocolServiceImpl extends BaseServiceImpl<ProtocolMapper, Protocol> implements IProtocolService {

    @Override
    @Transactional
    public BooleanResult save(Protocol entity) {
        this.getByCode(entity.getCode()).ifPresentThrow("协议代码已存在["+entity.getCode()+"]");

        if(entity.getImplMode() != ProtocolImplMode.Jar) {
            ProtocolDto protocolDto = loadProtocol(entity.getCode());

            if(protocolDto == null) {
                throw new ServiceException("没有找到对应的协议["+entity.getCode()+"]");
            }
        }

        return super.save(entity).ifPresentAndTrue(item -> {
            if(entity.getImplMode() == ProtocolImplMode.Jar) {
                DeviceProtocolSupplier supplier = ProtocolSupplierManager.copyTempToProtocol(entity.getJarPath());
                ProtocolSupplierManager.register(supplier);
            }
        });
    }

    @Override
    @Transactional
    public BooleanResult updateById(Protocol entity) {
        // 获取原协议对象
        Protocol protocol = this.getById(entity.getId()).ifNotPresentThrow("协议不存在").getData();

        this.getByCode(entity.getCode()).ifPresent(item -> {
            if(!item.getId().equals(entity.getId())) {
                throw new ServiceException("协议代码已存在["+entity.getCode()+"]");
            }
        });

        if(entity.getImplMode() != ProtocolImplMode.Jar) {
            ProtocolDto protocolDto = loadProtocol(entity.getCode());

            if(protocolDto == null) {
                throw new ServiceException("没有找到对应的协议["+entity.getCode()+"]");
            }
        }

        return super.updateById(entity).ifPresentAndTrue(item -> {
            if(entity.getImplMode() == ProtocolImplMode.Jar) {
                // 两个协议码不相同或者版本不一致
                if(!protocol.getCode().equals(entity.getCode()) || !protocol.getVersion().equals(entity.getVersion())) {
                    DeviceProtocolSupplier supplier = ProtocolSupplierManager.copyTempToProtocol(entity.getJarPath());
                    // 移除原来的协议提供
                    ProtocolSupplierManager.remove(protocol.getCode());
                    // 注册新的协议提供
                    ProtocolSupplierManager.register(supplier);
                }
            }
        });
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(!this.isBindGateway(idList)) {
            List<Protocol> protocols = this.listByIds(idList).ifNotPresentThrow("没有找到协议").getData();
            long count = protocols.stream().filter(item -> item.getImplMode() == ProtocolImplMode.Internal).count();
            if(count > 0) {
                return BooleanResult.buildFalse("系统内置协议不允许删除");
            }

            return super.removeByIds(idList).ifPresent(item -> {
                protocols.stream()
                        .filter(protocol -> protocol.getImplMode() == ProtocolImplMode.Jar)
                        .forEach(protocol -> ProtocolSupplierManager.remove(protocol.getCode()));
            });
        } else {
            return new BooleanResult(false, "此协议已被网关绑定");
        }
    }

    @Override
    public PageResult<IPage<ProtocolDto>> pageDetail(Page<Protocol> page, ProtocolDto entity) {
        return new PageResult<>(getBaseMapper().pageDetail(page, entity));
    }

    @Override
    public ProtocolDto loadProtocol(String code) {
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(code);
        return loadProtocol(supplier);
    }

    @Override
    public ProtocolDto loadProtocol(DeviceProtocolSupplier supplier) {
        if(supplier != null) {
            ProtocolDto protocolDto = new ProtocolDto();
            protocolDto.setDeviceConfig(supplier.getDeviceConfig());
            protocolDto.setProductConfig(supplier.getProductConfig());
            protocolDto.setGatewayConfig(supplier.getNetworkConfig());
            protocolDto.setGatewayType(supplier.getGatewayType());
            protocolDto.setConnectionType(supplier.getConnectionType());
            protocolDto.setVersion(supplier.getVersion());
            ProtocolModel protocol = supplier.getProtocol();
            protocolDto.setCode(protocol.getCode()).setRemark(protocol.getRemark())
                    .setImplMode(supplier.getImplMode()).setCtrlMode(protocol.getCtrlMode())
                    .setType(protocol.getType());
            // 协议接口
            protocol.getApis().values().stream().forEach(api -> {
                // 协议接口
                ProtocolApi protocolApi = new ProtocolApi()
                        .setProtocolCode(protocol.getCode())
                        .setCode(api.getCode())
                        .setName(api.getName())
                        .setType(api.getType())
                        .setFuncType(FuncType.R)
                        .setRemark(api.getRemark())
                        .setTriggerMode(api.getTriggerMode());

                // 功能类型
                if(api instanceof AbstractFuncProtocolModelApi) {
                    protocolApi.setFuncType(((AbstractFuncProtocolModelApi) api).getFuncType());
                }

                // 协议接口上行配置
                List<ProtocolApiConfig> upConfig = api.getUpConfig()
                        .values().stream().map(config -> {
                            ProtocolModelAttr attr = protocol.getAttrs().get(config.getProtocolModelAttrField());
                            // 将协议模型的api配置转换成协议api配置
                            return convertProtocolApiConfig(protocolApi, config, attr);
                        }).collect(Collectors.toList());

                // 协议接口下行配置
                List<ProtocolApiConfig> downConfig = api.getDownConfig()
                        .values().stream().map(config -> {
                            ProtocolModelAttr attr = protocol.getAttrs().get(config.getProtocolModelAttrField());
                            // 将协议模型的api配置转换成协议api配置
                            return convertProtocolApiConfig(protocolApi, config, attr);
                        }).collect(Collectors.toList());

                protocolDto.addApis(protocolApi.setUpConfigs(upConfig).setDownConfigs(downConfig));
            });

            // 协议属性
            List<ProtocolAttr> attrs = protocol.getAttrs().values().stream()
                    .map(attr -> new ProtocolAttr()
                            .setProtocolCode(protocol.getCode())
                            .setName(attr.getName())
                            .setField(attr.getField())
                            .setRemark(attr.getRemark())
                            .setDataType(attr.getDataType().getValue()))
                    .collect(Collectors.toList());
            protocolDto.setAttrs(attrs);

            return protocolDto;
        }

        return null;
    }

    @Override
    public Boolean isBindGateway(Collection<? extends Serializable> idList) {
        return getBaseMapper().isBindGateway(idList);
    }

    @Override
    public DetailResult<Protocol> getByCode(String code) {
        return getOne(Wrappers.<Protocol>lambdaQuery().eq(Protocol::getCode, code));
    }

    private ProtocolApiConfig convertProtocolApiConfig(ProtocolApi protocolApi, ProtocolModelApiConfig config, ProtocolModelAttr attr) {
        return new ProtocolApiConfig()
                .setSort(config.getSort())
                .setLength(config.getLength())
                .setOptions(config.getOptions())
                .setPosition(config.getPosition())
                .setFieldType(config.getFieldType())
                .setProtocolApiCode(protocolApi.getCode())
                .setAttrName(attr != null ? attr.getName() : null)
                .setDataType(attr != null ? attr.getDataType().getValue() : null)
                .setAttrRemark(attr != null ? attr.getRemark() : null)
                .setProtocolAttrField(config.getProtocolModelAttrField())
                .setRemark(StrUtil.isBlank(config.getRemark()) ? attr.getRemark() : config.getRemark());
    }

    @Override
    public DetailResult<ProtocolDto> getByDetail(Long id) {
        ProtocolDto detail = this.getBaseMapper().getByDetail(id);
        if(detail != null && detail.getImplMode() != ProtocolImplMode.Custom) {
            ProtocolDto protocolDto = this.loadProtocol(detail.getCode());
            if(protocolDto != null) {
                detail.setAttrs(protocolDto.getAttrs());
                detail.setFuncApis(protocolDto.getFuncApis());
                detail.setEventApis(protocolDto.getEventApis());
            }
        }

        return new DetailResult<>(detail);
    }
}
