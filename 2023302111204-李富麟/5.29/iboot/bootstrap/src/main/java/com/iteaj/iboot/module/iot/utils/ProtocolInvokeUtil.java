package com.iteaj.iboot.module.iot.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.UpModelAttr;
import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.DataType;
import com.iteaj.framework.spi.iot.consts.PointProtocolConfig;
import com.iteaj.framework.spi.iot.consts.TriggerMode;
import com.iteaj.framework.spi.iot.protocol.AbstractProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.framework.spring.SpringUtils;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.entity.ModelApiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProtocolInvokeUtil {

    private static IotCacheManager cacheManager;

    public static void invoke(ModelApi api, RealtimeStatus device, Consumer<InvokeResult> result) {
        invoke(api, device, ProtocolModelApiInvokeParam.DEFAULT_TIMEOUT, result);
    }

    public static void invoke(ModelApi api, ProtocolModelApiInvokeParam param, RealtimeStatus device, Consumer<InvokeResult> result) {
        invoke(ProtocolSupplierManager.get(device.getProtocolCode()), api, param, device, result);
    }

    public static void invoke(DeviceProtocolSupplier supplier, ModelApi api, ProtocolModelApiInvokeParam param, RealtimeStatus device, Consumer<InvokeResult> result) {
        if(supplier != null) {
            AbstractProtocolModelApi modelApi = supplier.getProtocol().getApi(api.getDirect());
            if(modelApi != null) {
                if(modelApi.getTriggerMode() == TriggerMode.active) {
                    throw new ServiceException("设备主动触发的协议不允许调用");
                }

                if(CollectionUtil.isNotEmpty(api.getUpConfig())) {
                    buildUpConfigAttrs(api, param);
                }
                if(CollectionUtil.isNotEmpty(api.getDownConfig())) {
                    api.getDownConfig().forEach(item -> {
                        // 如果是点位采集
                        resolverPointModelAttr(api, supplier, param, item);
                    });
                }

                // 组装调用参数
                initInvokeConfig(param, device);

                // 发起调用
                modelApi.invoke(param, result);
            } else {
                throw new ServiceException("没有找到协议指令[direct="+api.getDirect()+"]");
            }
        }
    }

    private static void initInvokeConfig(ProtocolModelApiInvokeParam param, RealtimeStatus device) {
        param.setDeviceSn(device.getDeviceSn())
                .setParentDeviceSn(device.getParentDeviceSn())
                .addDeviceConfig(device.getConfig());

        // 设置默认超时时间
        if(param.getTimeout() == null || param.getTimeout() <= 0) {
            param.setTimeout(ProtocolModelApiInvokeParam.DEFAULT_TIMEOUT);
        }

        if(device.getType() == DeviceType.Child) {
            IotCacheManager cacheManager = getCacheManager();
            RealtimeStatus parent = cacheManager.get(device.getProtocolCode(), device.getParentDeviceSn());
            if(parent != null) {
                param.addParentDeviceConfig(parent.getConfig());
            }
        }
    }

    private static IotCacheManager getCacheManager() {
        return cacheManager != null ? cacheManager : (cacheManager = SpringUtils.getBean(IotCacheManager.class));
    }

    public static void invoke(ModelApi api, RealtimeStatus device, long timeout, Consumer<InvokeResult> result) {
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(device.getProtocolCode());
        if(supplier != null) {
            AbstractProtocolModelApi modelApi = supplier.getProtocol().getApi(api.getDirect());
            if(modelApi != null) {
                if(modelApi.getTriggerMode() == TriggerMode.active) {
                    throw new ServiceException("设备主动触发的协议不允许调用");
                }

                JSONObject param = new JSONObject();
                ProtocolModelApiInvokeParam invokeParam = new ProtocolModelApiInvokeParam(device.getUid(), param);
                invokeParam.setTimeout(timeout > 0 ? timeout : ProtocolModelApiInvokeParam.DEFAULT_TIMEOUT);

                if(CollectionUtil.isNotEmpty(api.getUpConfig())) {
                    buildUpConfigAttrs(api, invokeParam);
                }

                if(CollectionUtil.isNotEmpty(api.getDownConfig())) {
                    api.getDownConfig().forEach(item -> {
                        if(item.getValue().startsWith("@") && item.getModelAttrId() != null) {
                            if(item.getAttrDefaultValue() == null) {
                                throw new ServiceException("协议/字段["+api.getName()+" / "+item.getAttrField()+"]没有默认值");
                            }

                            param.put(item.getProtocolAttrField(), item.getAttrDefaultValue());
                        } else {
                            param.put(item.getProtocolAttrField(), item.getValue());
                        }

                        // 如果是点位采集
                        resolverPointModelAttr(api, supplier, invokeParam, item);
                    });
                } else {
                    // 允许没有下行配置的接口
                }

                // 组装调用参数
                initInvokeConfig(invokeParam, device);

                // 发起调用
                modelApi.invoke(invokeParam, result);
            } else {
                throw new ServiceException("没有找到协议指令[direct="+api.getDirect()+"]");
            }
        }
    }

    private static void resolverPointModelAttr(ModelApi api, DeviceProtocolSupplier supplier, ProtocolModelApiInvokeParam invokeParam, ModelApiConfig item) {
        if (supplier.getProtocol().getCtrlMode() == CtrlMode.POINT) {
            if (Objects.equals(item.getProtocolAttrField(), PointProtocolConfig.POINT_ADDRESS)) {
                for (int i = 0; i < api.getUpConfig().size(); i++) {
                    ModelApiConfig apiConfig = api.getUpConfig().get(i); // 获取此点位地址绑定的物模型属性
                    if (Objects.equals(apiConfig.getProtocolAttrField(), PointProtocolConfig.POINT_VALUE)) {
                        if (apiConfig.getModelAttrId() != null && apiConfig.getAttrField() != null) {
                            invokeParam.addConfig(PointProtocolConfig.POINT_FIELD_KEY + item.getValue(), apiConfig.getAttrField());
                        } else {
                            throw new ServiceException("上行配置[protocolAttrName=" + apiConfig.getProtocolAttrName() + "]未指定物模型属性");
                        }
                    }
                }
            }
        }
    }

    private static void buildUpConfigAttrs(ModelApi api, ProtocolModelApiInvokeParam invokeParam) {
        List<UpModelAttr> upConfigAttrs = new ArrayList<>();
        api.getUpConfig().forEach(item -> {
            if(DataType.t_json.getValue().equals(item.getDataType())) {
                item.getDicts().forEach(dict -> {
                    upConfigAttrs.add(new UpModelAttr(dict.getModelAttrId(), dict.getAttrField(), dict.getDictName()
                            , DataType.build(dict.getDataType()), dict.getAccuracy(), dict.getGain(), dict.getResolver()
                            , dict.getScript(), dict.getPath()));
                });
            } else {
                upConfigAttrs.add(new UpModelAttr(item.getModelAttrId(), item.getAttrField(), item.getAttrName()
                        , DataType.build(item.getDataType()), item.getAccuracy(), item.getGain(), item.getResolver()
                        , item.getScript(), null));
            }
        });

        invokeParam.addUpAttrs(upConfigAttrs);
    }
}
