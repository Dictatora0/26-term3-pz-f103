package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.framework.spi.event.ApplicationReadyListener;
import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.framework.spi.iot.listener.IotEvenPublisher;
import com.iteaj.framework.spi.iot.listener.IotEventType;
import com.iteaj.iboot.module.iot.IBootThreadManger;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IModelApiService;
import com.iteaj.iboot.module.iot.utils.IotLogger;
import com.iteaj.iboot.module.iot.utils.ProtocolInvokeUtil;
import com.iteaj.iot.IotThreadManager;
import com.iteaj.iot.consts.ExecStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 物模型的设备在线状态协议管理
 * 一般用于网关子设备的状态采集
 */
public class DeviceStatusModelApiManager implements Runnable, ApplicationReadyListener {

    private IotCacheManager cacheManager;
    private IBootThreadManger bootThreadManger;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<Long, ModelApi> statusApis = new ConcurrentHashMap<>(16);

    private static DeviceStatusModelApiManager instance = new DeviceStatusModelApiManager();

    public static DeviceStatusModelApiManager getInstance() {
        return instance;
    }

    /**
     * 获取指定产品的状态api接口
     * @param productId
     * @return
     */
    public ModelApi get(Long productId) {
        return statusApis.get(productId);
    }

    /**
     * 注册api接口
     * @param api
     */
    public void register(ModelApi api) {
        if(!Boolean.TRUE.equals(api.getAsStatus())) {
            throw new IllegalArgumentException("必须是状态接口[asStatus == true]");
        }

        if(!statusApis.containsKey(api.getProductId())) {
            statusApis.put(api.getProductId(), api);
        }
    }

    /**
     * 更新接口
     * @param newModelApi
     * @return
     */
    public ModelApi update(ModelApi newModelApi) {
        if(!Boolean.TRUE.equals(newModelApi.getAsStatus())) {
            throw new IllegalArgumentException("必须是[asStatus == true]接口");
        }

        return statusApis.put(newModelApi.getProductId(), newModelApi);
    }

    /**
     * 移除接口
     * @param productId
     * @return
     */
    public ModelApi remove(Long productId) {
        return statusApis.remove(productId);
    }

    /**
     * 获取所有的状态位接口
     * @return
     */
    public Collection<ModelApi> apis() {
        return Collections.unmodifiableCollection(statusApis.values());
    }

    /**
     * 触发指定产品状态更新
     * @param productCode 产品代码
     */
    public void trigger(String productCode) {
        statusApis.forEach((key, api) -> {
            if(Objects.equals(productCode, api.getProductCode())) {
                cacheManager.listByProductCode(api.getProductCode()).forEach(device -> {
                    doTrigger(api, device);
                });
            }
        });
    }

    /**
     * 触发指定产品下的设备状态更新
     * @param uid 设备uid
     * @param productCode 产品代码
     */
    public void trigger(String productCode, String uid) {
        statusApis.forEach((key, api) -> {
            if(Objects.equals(productCode, api.getProductCode())) {
                cacheManager.listByProductCode(api.getProductCode()).forEach(device -> {
                    if(Objects.equals(device.getUid(), uid)) {
                        doTrigger(api, device);
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        String prevDeviceSn = "";
        Iterator<ModelApi> apiIterator = statusApis.values().iterator();
        while (apiIterator.hasNext()) {
            ModelApi modelApi = apiIterator.next();
            // 获取产品下面的所有设备, 并且执行这些设备的状态协议
            Collection<RealtimeStatus> statuses = cacheManager.listByProductCode(modelApi.getProductCode());
            Iterator<RealtimeStatus> iterator = statuses.iterator();
            while (iterator.hasNext()) {
                RealtimeStatus device = iterator.next();
                if(prevDeviceSn != null && ProtocolCodes.isModbus(device.getProtocolCode())) {
                    // 同一台网关设备的采集时间需要有一定间隔(防止网关设备性能差导致采集失败)
                    if(prevDeviceSn.equals(device.getParentDeviceSn())) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {

                        }
                    }
                }

                doTrigger(modelApi, device);
                prevDeviceSn = device.getParentDeviceSn();
            }
        }
    }

    private void doTrigger(ModelApi api, RealtimeStatus device) {
        try {
            if(device.getType() == DeviceType.Child) { // 先校验父设备是否在线
                RealtimeStatus realtimeStatus = cacheManager.get(device.getProtocolCode(), device.getParentDeviceSn());
                // 父设备不存在或者不在线则不采集子设备状态
                if(realtimeStatus == null || realtimeStatus.getStatus() == DeviceStatus.offline) {
                    IotLogger.debug(IotLogger.NAME, "采集状态", device.getUid(), "放弃状态接口调用 网关设备不在线 - 网关编号: {} - 设备编号: {} - 接口: {}/{}"
                            , device.getParentDeviceSn(), device.getDeviceSn(), api.getCode(), api.getDirect());
                    return;
                }
            }

            // 调用状态协议
            doInvoke(api, device, 3000l, false);
        } catch (Exception e) {
            invokeException(api, device, e);
        }
    }

    private void invokeException(ModelApi api, RealtimeStatus device, Exception e) {
        // 获取当前设备的上一次状态
        if(device.getStatus() != DeviceStatus.offline) {
            device.setStatus(DeviceStatus.offline);
            IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
        }

        IotLogger.error(IotLogger.NAME, "采集状态", device.getUid(), "采集状态异常 {} - 设备编号: {} - 接口: {}/{}"
                , e.getMessage(), device.buildDeviceKey(), api.getCode(), api.getDirect(), e);
    }

    private void doInvoke(ModelApi api, RealtimeStatus device, long timeout, boolean retry) {
        ProtocolInvokeUtil.invoke(api, device, timeout, result -> {
            DeviceStatus realtimeStatus;
            if(result.getStatus() == ExecStatus.success) {
                boolean status = true;
//                    Object value = result.getValue();
//                    if(value instanceof Boolean) { // 如果是布尔值必须等于true
//                        status = Boolean.TRUE.equals(value);
//                    } else if(value instanceof Number){ // 如果是数值必须大于0
//                        Double aDouble = Double.valueOf(value.toString());
//                        status = aDouble != 0.0;
//                    }

                realtimeStatus = status == true ? DeviceStatus.online : DeviceStatus.offline;
                // 如果现在的状态和上一次的状态不一致则更新状态
                if(realtimeStatus != device.getStatus()) {
                    device.setStatus(realtimeStatus);
                    IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
                }

            } else {
                // 如果执行超时则重新尝试一次
                if(result.getStatus() == ExecStatus.timeout && !retry) {
                    this.doInvoke(api, device, 5000l, true);
                } else {
                    // 如果现在的状态和上一次的状态不一致则更新状态
                    if(DeviceStatus.offline != device.getStatus()) {
                        device.setStatus(DeviceStatus.offline);
                        IotEvenPublisher.publish(IotEventType.DeviceStatus, device);
                    }

                    if(logger.isWarnEnabled()) {
                        if(retry) {
                            IotLogger.warn(IotLogger.NAME, "采集状态", device.getUid(), "采集状态重试失败 {} - 设备编号: {} - 接口: {}/{}"
                                    , result.getStatus(), device.buildDeviceKey(), api.getCode(), api.getDirect());
                        } else {
                            IotLogger.warn(IotLogger.NAME, "采集状态", device.getUid(), "采集状态失败 {} - 设备编号: {} - 接口: {}/{}"
                                    , result.getStatus(), device.buildDeviceKey(), api.getCode(), api.getDirect());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void started(ApplicationContext context) {
        this.cacheManager = context.getBean(IotCacheManager.class);
        this.bootThreadManger = context.getBean(IBootThreadManger.class);
        IModelApiService modelApiService = context.getBean(IModelApiService.class);
        modelApiService.listAsStatusModelApi().forEach(api -> this.register(api));

        /**
         * 30秒执行一次
         */
        bootThreadManger.getDeviceStatusScheduler()
                .scheduleWithFixedDelay(this, 15, 35, TimeUnit.SECONDS);
    }
}
