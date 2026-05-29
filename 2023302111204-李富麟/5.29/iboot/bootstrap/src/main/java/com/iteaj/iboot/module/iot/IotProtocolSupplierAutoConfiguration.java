package com.iteaj.iboot.module.iot;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.iteaj.iboot.module.iot.service.IProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 协议提供配置
 */
public class IotProtocolSupplierAutoConfiguration {

    private final FrameworkProperties properties;

    public IotProtocolSupplierAutoConfiguration(FrameworkProperties properties) {
        this.properties = properties;
    }

    /**
     * 协议提供管理
     * @return
     */
    @Bean
    public ProtocolSupplierManager protocolSupplierManager(@Autowired(required = false) List<DeviceProtocolSupplier> suppliers, IProtocolService protocolService) {
        ProtocolSupplierManager supplierManager = ProtocolSupplierManager.build(properties);
        // 注册内置协议
        if(CollectionUtil.isNotEmpty(suppliers)) {
            suppliers.forEach(item -> ProtocolSupplierManager.register(item));
        }

        // 注册jar spi协议
        protocolService.list(Wrappers.<Protocol>lambdaQuery().eq(Protocol::getImplMode, ProtocolImplMode.Jar)).forEach(protocol -> {
            List<DeviceProtocolSupplier> jarSupplier = ProtocolSupplierManager.loadFromProtocol(protocol.getJarPath());
            if(CollectionUtil.isNotEmpty(jarSupplier)) {
                jarSupplier.forEach(item -> ProtocolSupplierManager.register(item));
            }
        });

        return supplierManager;
    }

}
