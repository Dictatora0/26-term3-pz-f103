package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.NetworkConfig;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.ConstConfig;
import com.iteaj.framework.spi.iot.consts.GatewayType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.framework.spi.iot.listener.EntityPayload;
import com.iteaj.framework.spi.iot.listener.IotEvenPublisher;
import com.iteaj.framework.spi.iot.listener.IotEventType;
import com.iteaj.framework.spi.iot.listener.JsonPayload;
import com.iteaj.iboot.module.iot.consts.GatewayStatus;
import com.iteaj.iboot.module.iot.dto.GatewayDto;
import com.iteaj.iboot.module.iot.entity.Gateway;
import com.iteaj.iboot.module.iot.mapper.GatewayMapper;
import com.iteaj.iboot.module.iot.service.IGatewayService;
import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.FrameworkManager;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 协议网关 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-12
 */
@Service
public class GatewayServiceImpl extends BaseServiceImpl<GatewayMapper, Gateway> implements IGatewayService {

    @Override
    public BooleanResult save(Gateway entity) {
        Long protocolId = entity.getProtocolId();
        this.getByProtocolId(protocolId).ifPresentThrow("应用协议已被绑定");
        if(entity.getConnectType() == ConnectionType.Server) {
            Integer port = entity.getConfig().getInteger(ConstConfig.PORT);
            if(port == null) {
                throw new ServiceException("未指定监听端口");
            }

            if(this.getBaseMapper().getByPort(port) != null) {
                throw new ServiceException("端口["+port+"]已被使用");
            }
        }

        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(Gateway entity) {
        Long protocolId = entity.getProtocolId();
        this.getOne(Wrappers.<Gateway>lambdaQuery()
                .ne(Gateway::getId, entity.getId())
                .eq(Gateway::getProtocolId, protocolId))
                .ifPresentThrow("应用协议已被绑定");
        if(entity.getConnectType() == ConnectionType.Server) {
            Integer port = entity.getConfig().getInteger(ConstConfig.PORT);
            if(port == null) {
                throw new ServiceException("未指定监听端口");
            }

            Gateway byPort = this.getBaseMapper().getByPort(port);
            if(byPort != null && byPort.getId() != entity.getId()) {
                throw new ServiceException("端口["+port+"]已被使用");
            }
        }

        return super.updateById(entity);
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtil.isEmpty(idList)) {
            return BooleanResult.buildFalse("未指定要删除的网关");
        }

        this.list(Wrappers.<Gateway>lambdaQuery()
            .in(Gateway::getId, idList))
            .forEach(gateway -> {
                if(gateway.getStatus() == GatewayStatus.start) {
                    throw new ServiceException("请先停止网关["+gateway.getName()+"]后再删除");
                }

                if(this.isBindProduct(gateway.getId())) {
                    throw new ServiceException("此网关["+gateway.getName()+"]已被绑定");
                }
            });

        return super.removeByIds(idList);
    }

    @Override
    public boolean isBindProduct(Long gatewayId) {
        return this.getBaseMapper().isBindProduct(gatewayId);
    }

    @Override
    public DetailResult<GatewayDto> detailById(Long id) {
        return new DetailResult<>(this.getBaseMapper().detailById(id));
    }

    @Override
    public DetailResult<GatewayDto> getByProtocolId(Long protocolId) {
        return new DetailResult<>(this.getBaseMapper().getByProtocolId(protocolId));
    }

    @Override
    public ListResult<IVOption> protocols(GatewayType type, TransportProtocol protocol, ConnectionType connectionType) {
        List<IVOption> optionList = getBaseMapper().protocols(type, protocol, connectionType);
        return new ListResult<>(optionList);
    }

    @Override
    public void switchStatus(Long id, GatewayStatus status) {
        if(status == null) {
            throw new ServiceException("未指定切换状态");
        }

        this.detailById(id).ifNotPresentThrow("网关不存在").ifPresent(item -> {
            this.switchStatus(item, status);
        });
    }

    @Override
    public void switchStatus(GatewayDto gatewayDto, GatewayStatus status) {
        this.doSwitchStatus(gatewayDto, status);
        this.update(Wrappers.<Gateway>lambdaUpdate()
                .set(Gateway::getStatus, status)
                .set(Gateway::getReason, "")
                .eq(Gateway::getId, gatewayDto.getId()));

        // 发布网关状态切换事件
        IotEvenPublisher.publish(IotEventType.GatewaySwitch, new EntityPayload(gatewayDto.setStatus(status)));
    }

    private void doSwitchStatus(GatewayDto gateway, GatewayStatus status) {
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(gateway.getProtocolCode());
        if(supplier == null) {
            throw new ServiceException("未找到对应的协议["+gateway.getProtocolCode()+"]");
        }

        FrameworkComponent component;
        if(supplier.getConnectionType() == ConnectionType.Server) {
            NetworkConfig config = gateway.getConfig().toJavaObject((Type) supplier.getNetworkConfigClazz());
            component = supplier.createComponent(config);
        } else {
            component = supplier.createComponent(null);
        }

        // 执行开启的操作
        if(status == GatewayStatus.start) {
            FrameworkManager.start(component);
        } else if(status == GatewayStatus.stop) { // 执行停止操作
            FrameworkManager.stop(component);
        }
    }

}
