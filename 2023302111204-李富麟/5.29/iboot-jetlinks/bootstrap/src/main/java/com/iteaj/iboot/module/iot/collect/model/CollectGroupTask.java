package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.ProtocolInvokeException;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.consts.ProtocolCodes;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectListenerManager;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.utils.IotLogger;
import com.iteaj.iboot.module.iot.utils.ProtocolInvokeUtil;
import com.iteaj.iot.consts.ExecStatus;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectGroupTask implements Runnable{

    private DeviceGroup group;
    private List<ModelApi> apis;
    private EventSourceDto source;
    private IotCacheManager cacheManager;

    public CollectGroupTask(DeviceGroup group, EventSourceDto source, IotCacheManager cacheManager) {
        this.group = group;
        this.source = source;
        this.apis = source.getApis();
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        // 没有任何采集接口, 放弃采集任务
        if(this.apis == null || this.apis.isEmpty()) {
            IotLogger.warn(source,"-","放弃采集任务 没有任何采集接口 - 设备组: {}", group.getName());
            return;
        }

        String prevParentDeviceSn = null;
        CollectInfo info = new CollectInfo(source, group);
        Collection<RealtimeStatus> devices = cacheManager.listByGroupId(group.getId());
        Iterator<RealtimeStatus> iterator = devices.iterator();
        while (iterator.hasNext()) {
            RealtimeStatus device = iterator.next();
            // 当前设备的产品不在事件源包含的产品里面
            if(device.getProductId() == null || !source.getProductIds().contains(device.getProductId().intValue())) {
                continue;
            }

            if(device.getStatus() == DeviceStatus.offline) {
                if(device.getType() == DeviceType.Gateway) {
                    IotLogger.warn(source, device.getUid(), "放弃设备采集 设备不在线 - 设备组: {} - 设备编号: {}"
                            , group.getName(), device.buildDeviceKey());
                    continue;
                } else if(device.getType() == DeviceType.Child) {
                    RealtimeStatus parent = cacheManager.get(device.getProtocolCode(), device.getParentDeviceSn());
                    if(parent != null) {
                        if(parent.getStatus() == DeviceStatus.offline) {
                            IotLogger.warn(source, device.getUid(), "放弃设备采集 网关设备不在线 - 设备组: {} - 设备编号: {}"
                                    , group.getName(), device.buildDeviceKey());
                            continue;
                        }
                    } else {
                        IotLogger.warn(source, device.getUid(), "放弃设备采集 父设备不存在 - 设备组: {} - 设备编号: {}"
                                , group.getName(), device.buildDeviceKey());
                        continue;
                    }
                }

            }

            Iterator<ModelApi> apiIterator = apis.iterator();
            while (apiIterator.hasNext()) {
                ModelApi api = apiIterator.next();
                try {
                    // 对此设备同一产品的模型接口进行调用
                    if(api.getProductId().equals(device.getProductId())) {
                        if(prevParentDeviceSn != null && ProtocolCodes.isModbus(device.getProtocolCode())) {
                            // 同一台网关设备的采集时间需要有一定间隔(防止网关设备性能差导致采集失败)
                            if(prevParentDeviceSn.equals(device.getParentDeviceSn())) {
                                Thread.sleep(1000); // 休眠1秒
                            }
                        }

                        doInvoke(info, device, api, 3000l, false);
                        prevParentDeviceSn = device.getParentDeviceSn();
                    }
                } catch (ProtocolInvokeException e) {
                    IotLogger.error(source, device.getUid(),"接口采集执行异常 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", e.getMessage()
                            , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect(), e);
                } catch (ServiceException e) {
                    IotLogger.error(source, device.getUid(),"接口采集执行错误 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", e.getMessage()
                            , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect(), e);
                } catch (Exception e) {
                    IotLogger.error(source, device.getUid(),"接口采集未知异常 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", e.getMessage()
                            , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect(), e);
                }
            }
        }

        CollectListenerManager.getInstance().eventGroupPublish(listener -> {
            try {
                listener.finished(info);
            } catch (Exception e) {
                IotLogger.error(source,"-","采集事件完成发布异常 {} - 设备组: {}", e.getMessage(), source.getName(), group.getName(), e);
            }
        });
    }

    private void doInvoke(CollectInfo info, RealtimeStatus device, ModelApi api, long timeout, boolean retry) {
        ProtocolInvokeUtil.invoke(api, device, timeout, result -> {
            if(result.getStatus() == ExecStatus.success) {
                if(api.getFuncType() != FuncType.W) { // 单纯写的功能没有采集数据
                    // 发布采集的数据
                    dataPublish(info, device, api, result);
                }
            } else {
                if(result.getStatus() == ExecStatus.timeout && !retry) {
                    this.doInvoke(info, device, api, 6000l, true);
                } else {
                    if(retry) {
                        IotLogger.warn(source, device.getUid(), "采集重试失败 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", result.getReason()
                                , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect());
                    } else {
                        IotLogger.warn(source, device.getUid(), "采集失败 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", result.getReason()
                                , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect());
                    }

                    // 发布采集的数据
                    dataPublish(info, device, api, result);
                }
            }
        });
    }

    private void dataPublish(CollectInfo info, RealtimeStatus device, ModelApi api, InvokeResult result) {
        ProtocolSupplierManager.getDataSupplier(result.getProtocol()).ifPresent(data -> {
            CollectInfo.DeviceInfo deviceInfo = info.addDeviceInfo(device, api, data);
            IotLogger.debug(source, device.getUid(), "采集数据成功 设备编号: {}", device.buildDeviceKey());
            CollectListenerManager.getInstance().eventGroupPublish(item -> {
                try {
                    item.supplier(source, group, deviceInfo);
                } catch (Exception e) {
                    IotLogger.error(source, device.getUid(), "采集事件实时发布异常 {} - 设备组: {} - 设备编号: {} - 接口: {}/{}", e.getMessage()
                            , group.getName(), device.buildDeviceKey(), api.getCode(), api.getDirect(), e);
                }
            });
        });
    }
}
