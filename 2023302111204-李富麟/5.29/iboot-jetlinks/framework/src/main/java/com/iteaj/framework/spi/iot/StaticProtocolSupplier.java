package com.iteaj.framework.spi.iot;

import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.FrameworkManager;

import java.util.Objects;

/**
 * 静态协议实现
 */
public abstract class StaticProtocolSupplier<C extends NetworkConfigImpl, T extends FrameworkComponent> implements DeviceProtocolSupplier<C, T> {

    private T component;

    @Override
    public T getComponent() {
        return component;
    }

    @Override
    public synchronized T createComponent(C config) {
        if(component == null) {
            component = doCreateComponent(config);
            FrameworkManager.register(component);
            ProtocolSupplierManager.register(component.getMessageClass(), this);
        } else {
            doRefresh(component, config);
        }

        return component;
    }

    /**
     * 重新刷新组件
     * @param component
     * @param config
     */
    protected void doRefresh(T component, C config) {}

    /**
     * 创建组件
     * @param config
     * @return
     */
    protected abstract T doCreateComponent(C config);

    /**
     * 属性值数据解析
     * @param attr
     * @param value
     * @return
     */
    protected Object dataResolver(UpModelAttr attr, Object value) {
        DataValueResolver resolver = DataValueResolverFactory.getDefault();
        if(attr.getResolver() != null && attr.getResolver().length() > 0) {
            resolver = DataValueResolverFactory.get(attr.getResolver());
        }

        if(resolver == null) {
            if(supplierLogger.isWarnEnabled()) {
                supplierLogger.warn("未找到数据解析器({}) {} - 字段: {} - 值: {}", attr.getResolver(), getDesc(), attr.getField(), value);
            }

            resolver = DataValueResolverFactory.getDefault();
        }

        return resolver.resolver(attr, value);
    }

    @Override
    public boolean equals(Object obj) {
        DeviceProtocolSupplier supplier = (DeviceProtocolSupplier) obj;
        return Objects.equals(supplier.getProtocol().getCode(), this.getProtocol().getCode());
    }
}
