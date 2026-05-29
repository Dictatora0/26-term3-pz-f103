package com.iteaj.framework.spi.iot;

import cn.hutool.core.util.ClassUtil;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.GatewayType;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.framework.spi.iot.protocol.ProtocolModel;
import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协议提供
 */
public interface DeviceProtocolSupplier<C extends NetworkConfig, T extends FrameworkComponent> {

    Logger supplierLogger = LoggerFactory.getLogger(DeviceProtocolSupplier.class);

    /**
     * 协议描述
     * @return
     */
    String getDesc();

    /**
     * 协议版本
     * @return
     */
    String getVersion();

    /**
     * 返回协议配置
     * @return
     */
    ProtocolModel getProtocol();

    /**
     * 网关类型
     * @return
     */
    GatewayType getGatewayType();

    /**
     * 连接类型
     * @return
     */
    ConnectionType getConnectionType();

    /**
     * 实现方式
     * @return
     */
    ProtocolImplMode getImplMode();

    ParamMeta[] EMPTY_CONFIG = new ParamMeta[]{};

    /**
     * 产品额外配置
     * @return
     */
    default ParamMeta[] getProductConfig() {
        return EMPTY_CONFIG;
    }

    /**
     * 子设备配置
     * @return
     */
    default ParamMeta[] getDeviceConfig() {
        return EMPTY_CONFIG;
    }

    /**
     * 网关设备配置
     * @return
     */
    default ParamMeta[] getGatewayConfig() {
        return EMPTY_CONFIG;
    }

    /**
     * 网络组件额外配置
     * @return
     */
    default ParamMeta[] getNetworkConfig() {
        return EMPTY_CONFIG;
    }

    /**
     * 返回网络配置类型
     * @return
     */
    default Class<C> getNetworkConfigClazz() {
        return (Class<C>) ClassUtil.getTypeArgument(getClass(), 0);
    }

    /**
     * 获取协议提供的数据
     * @param protocol
     * @param param {@link org.springframework.lang.Nullable} 平台主动调用时的参数
     * @param value 协议请求返回值
     * @return
     */
    DataSupplier getDataSupplier(Protocol protocol, ProtocolModelApiInvokeParam param, Object value);

    /**
     * 获取已经创建的组件
     * @return
     */
    T getComponent();

    /**
     * 创建组件
     * @param config 服务端组件必填
     * @return 如果已经存在直接返回, 不存在则创建
     */
    T createComponent(C config);
}
