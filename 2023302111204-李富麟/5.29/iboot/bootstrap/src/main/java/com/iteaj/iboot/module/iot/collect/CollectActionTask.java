package com.iteaj.iboot.module.iot.collect;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolModelApiInvokeParam;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.CollectStatus;
import com.iteaj.framework.spi.iot.consts.PointProtocolConfig;
import com.iteaj.framework.spi.iot.protocol.AbstractProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.NotIotClientException;
import com.iteaj.framework.spi.iot.protocol.ProtocolModel;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.store.StoreAction;
import com.iteaj.iboot.module.iot.collect.store.StoreActionFactory;
import com.iteaj.iboot.module.iot.consts.CollectMode;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.dto.CollectTaskDto;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iot.consts.ExecStatus;
import com.iteaj.iot.utils.UniqueIdGen;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CollectActionTask implements Runnable {

    private CollectTaskDto taskDto;
    private IotCacheManager cacheManager;
    private Logger logger = LoggerFactory.getLogger("IOT:SIGNAL:COLLECT");

    public CollectActionTask(CollectTaskDto taskDto, IotCacheManager cacheManager) {
        this.taskDto = taskDto;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        if(taskDto != null && !CollectionUtils.isEmpty(taskDto.getDetails())) {
            Date date = new Date(); // 任务开始时间
            taskDto.getDetails().forEach(item -> {
                long cid = UniqueIdGen.nextLong();
                List<CollectDevice> devices = item.getDevices();
                if(CollectionUtil.isNotEmpty(devices)) {
                    devices.forEach(collectDevice -> {
                        RealtimeStatus realtimeStatus = cacheManager.get(collectDevice.getProtocolCode(), collectDevice.buildKey());
                        if(realtimeStatus != null) {
//                            if(realtimeStatus.getStatus() == DeviceStatus.online) {
                                List<CollectData> values = new ArrayList<>();
                                DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(collectDevice.getProtocolCode());
                                if(supplier != null) {
                                    ProtocolModel protocol = supplier.getProtocol();
                                    collectDevice.getSignals().forEach(signal -> {
                                        CollectData data = new CollectData(CollectMode.signal)
                                                .setCid(cid + "").setReason("")
                                                .setFieldName(signal.getFieldName())
                                                .setAddress(signal.getAddress())
                                                .setCollectStatus(CollectStatus.Success)
                                                .setCreateTime(date)
                                                .setUid(String.valueOf(collectDevice.getUid()))
                                                .setSignalId(signal.getId())
                                                .setCollectTaskId(taskDto.getId());

                                        try {
                                            AbstractProtocolModelApi modelApi = protocol.getApi(signal.getDirect());
                                            JSONObject param = new JSONObject();
                                            param.put(PointProtocolConfig.POINT_NUMBER, signal.getNum());
                                            param.put(PointProtocolConfig.POINT_ADDRESS, signal.getAddress());
                                            param.put(PointProtocolConfig.POINT_CHILD_SN, collectDevice.getDeviceSn());
                                            ProtocolModelApiInvokeParam invokeParam = new ProtocolModelApiInvokeParam(collectDevice.getUid().toString(), param)
                                                    .setDeviceSn(collectDevice.getDeviceSn()).setParentDeviceSn(collectDevice.getParentDeviceSn());

                                            modelApi.invoke(invokeParam, invokeResult -> {
                                                data.setCollectTime(new Date());
                                                if(invokeResult.getStatus() == ExecStatus.success) {
                                                    data.setValue(invokeResult.getValue().toString());
                                                } else {
                                                    data.setCollectStatus(CollectStatus.from(invokeResult.getStatus()));
                                                }
                                            });
                                        } catch (CollectException e) {
                                            String message = e.getMessage() == null ? e.getCause().getMessage() : e.getMessage();
                                            data.setCollectStatus(CollectStatus.Fail).setCollectTime(new Date()).setReason(message);
                                            logger.error("采集任务失败 {} - 任务: {} - 周期: {} - 设备: {} - 点位: {}({})"
                                                    , message, taskDto.getName(), taskDto.getCron()
                                                    , collectDevice.getDeviceSn(), signal.getName(), signal.getAddress(), e);
                                        } catch (NotIotClientException e) {
                                            data.setCollectStatus(CollectStatus.Fail).setCollectTime(new Date()).setReason("获取不到客户端");
                                            logger.error("采集任务失败 {} - 任务: {} - 周期: {} - 设备: {} - 点位: {}({})"
                                                    , e.getMessage(), taskDto.getName(), taskDto.getCron()
                                                    , collectDevice.getDeviceSn(), signal.getName(), signal.getAddress(), e);
                                        } catch (Exception e) {
                                            data.setCollectStatus(CollectStatus.Fail).setCollectTime(new Date()).setReason("未知错误");
                                            logger.error("采集任务失败 {} - 任务: {} - 周期: {} - 设备: {} - 点位: {}({})"
                                                    , e.getMessage(), taskDto.getName(), taskDto.getCron()
                                                    , collectDevice.getDeviceSn(), signal.getName(), signal.getAddress(), e);
                                        }

                                        values.add(data);
                                        CollectListenerManager.getInstance().signalPublish(listener -> {
                                            try {
                                                listener.supplier(collectDevice, data);
                                            } catch (Exception e) {
                                                logger.error("采集点位事件实时发布异常 - 任务: {} - 设备: {}", taskDto.getName(), collectDevice.buildKey(), e);
                                            }
                                        });
                                    });
                                }

                                // 存储
                                if(!CollectionUtils.isEmpty(values)) {
                                    StoreAction storeAction = StoreActionFactory.getInstance().get(item.getStoreAction());
                                    if(storeAction != null) {
                                        storeAction.store(item, values);
                                    }

                                    // 存储实时数据
                                    CollectListenerManager.getInstance().signalPublish(listener -> {
                                        try {
                                            listener.finished(collectDevice, values);
                                        } catch (Exception e) {
                                            logger.error("采集点位事件完成发布异常 - 任务: {} - 设备: {}", taskDto.getName(), collectDevice.buildKey(), e);
                                        }
                                    });
                                }
//                            } else {
//                                logger.warn("采集点位取消 设备不在线 - 任务: {} - 设备: {}", taskDto.getName(), collectDevice.buildKey());
//                            }
                        } else {
                            logger.warn("采集点位取消 设备不存在 - 任务: {} - 设备: {}", taskDto.getName(), collectDevice.buildKey());
                        }
                    });
                }
            });
        } else {
            logger.error("采集任务失败 任务不存在 - 任务: {} - cron: {}", taskDto.getName(), taskDto.getCron());
        }

    }
}
