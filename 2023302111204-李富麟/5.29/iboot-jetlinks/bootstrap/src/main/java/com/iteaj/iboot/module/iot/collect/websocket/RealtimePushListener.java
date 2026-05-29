package com.iteaj.iboot.module.iot.collect.websocket;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.spi.iot.DataSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.SignalOrFieldValue;
import com.iteaj.framework.spi.iot.consts.FuncType;
import com.iteaj.framework.spi.iot.protocol.InvokeResult;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.cache.entity.RealtimeStatus;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.collect.DeviceStatusListener;
import com.iteaj.iboot.module.iot.collect.SignalCollectListener;
import com.iteaj.iboot.module.iot.collect.model.CollectInfo;
import com.iteaj.iboot.module.iot.collect.model.EventGroupCollectListener;
import com.iteaj.iboot.module.iot.collect.model.ModelAttrListener;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.utils.IotLogger;
import com.iteaj.iot.IotThreadManager;
import com.iteaj.iot.server.websocket.WebSocketChannelMatcher;
import com.iteaj.iot.server.websocket.WebSocketServerListener;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 将采集的数据实时推送到前端
 */
public class RealtimePushListener implements WebSocketServerListener, EventGroupCollectListener
        , SignalCollectListener, DeviceStatusListener, ModelAttrListener {

    private static final String URI = "/ws/iot/realtime";
    private final RealtimeDataService realtimeDataService;
    /**
     * 监听的告警类型, 可以组合比如：12 表示监听告警和状态
     */
    private final static String TYPE_WARN = "1"; // 实时告警
    private final static String TYPE_STATUS = "2"; // 实时状态
    private final static String TYPE_DATA = "3"; // 实时数据

    private final static String DEVICE_ALL = "-1"; // 监听所有设备

    private Logger logger = LoggerFactory.getLogger(getClass());

    public RealtimePushListener(RealtimeDataService realtimeDataService) {
        this.realtimeDataService = realtimeDataService;
    }

    @Override
    public String uri() {
        return URI;
    }

    @Override
    public void onText(DefaultWebSocketServerProtocol protocol) {

    }

    @Override
    public void supplier(EventSource source, DeviceGroup group, CollectInfo.DeviceInfo info) {
        IotThreadManager.instance().getDeviceManageEventExecutor().execute(() -> this.push(info));
    }

    @Override
    public void supplier(RealtimeStatus device) {
        this.supplier(Arrays.asList(device));
    }

    @Override
    public void supplier(Collection<RealtimeStatus> devices) {
        if(CollectionUtil.isNotEmpty(devices)) {
            devices.forEach(device -> {
                try {
                    IotLogger.info(device.getStatus(), device.getUid(), "设备{} - 设备编号: {}"
                            , device.getStatus() == DeviceStatus.online ? "上线" : "离线", device.getDeviceSn());
                    String jsonString = JSONObject.toJSONString(PushData.buildStatus(device));
                    doPush(device.getUid(), jsonString, TYPE_STATUS);
                } catch (Exception e) {
                    logger.error("推送实时状态异常 {} - 设备: {}", e.getMessage(), device.buildDeviceKey(), e);
                }
            });
        }
    }

    /**
     * 写出数据
     * @param device
     */
    public void push(CollectInfo.DeviceInfo device) {
        try {
            if(CollectionUtil.isNotEmpty(device.getSupplier().getValues())) {
                Map<String, SignalOrFieldValue> valueMap = device.getSupplier().getValues().stream()
                        .collect(Collectors.toMap(SignalOrFieldValue::getSignalOrField, item -> item));

                String jsonString = JSONObject.toJSONString(PushData.buildModel(device.getDevice().getUid(), valueMap));
                doPush(device.getDevice().getUid(), jsonString, TYPE_DATA);
            }
        } catch (Exception e) {
            logger.error("实时推送模型数据异常 {} - 设备: {}", e.getMessage(), device.getDevice().buildDeviceKey(), e);
        }
    }

    public void push(String deviceUid, ModelApi api, InvokeResult result) {
        if(api.getFuncType() == FuncType.R) { // 读指令才需要更新数据
            ProtocolSupplierManager.getDataSupplier(result.getProtocol()).ifPresent(supplier -> {
                if(CollectionUtil.isNotEmpty(supplier.getValues())) {
                    Map<String, SignalOrFieldValue> valueMap = supplier.getValues().stream()
                            .collect(Collectors.toMap(SignalOrFieldValue::getSignalOrField, item -> item));
                    realtimeDataService.put(supplier); // 加入实时数据缓存

                    // websocket方式写数据到前端
                    String jsonString = JSONObject.toJSONString(PushData.buildModel(deviceUid, valueMap));
                    doPush(deviceUid, jsonString, TYPE_DATA);
                }
            });
        }
    }

    @Override
    public void supplier(CollectDevice device, CollectData data) {
        try {
            Map<String, SignalOrFieldValue> valueMap = new HashMap<>();
            valueMap.put(data.getAddress(), SignalOrFieldValue.build(data.getSignalId()
                    , data.getAddress(), data.getCollectTime(), data.getValue()).builder(data.getCollectStatus()));
            String deviceUid = device.getUid().toString();
            String jsonString = JSONObject.toJSONString(PushData.buildSignal(deviceUid, valueMap));

            doPush(deviceUid, jsonString, TYPE_DATA);
        } catch (Exception e) {
            logger.error("实时推送点位数据异常 {} - 设备: {}", e.getMessage(), device.buildKey(), e);
        }
    }

    @Override
    public void supplier(DataSupplier supplier, RealtimeStatus device) {
        if(CollectionUtil.isNotEmpty(supplier.getValues())) {
            Map<String, SignalOrFieldValue> valueMap = supplier.getValues().stream()
                    .collect(Collectors.toMap(SignalOrFieldValue::getSignalOrField, item -> item));

            // websocket方式写数据到前端
            String jsonString = JSONObject.toJSONString(PushData.buildModel(device.getUid(), valueMap));
            doPush(device.getUid(), jsonString, TYPE_DATA);
        }
    }

    private void doPush(String deviceUid, String jsonString, String typeData) {
        DefaultWebSocketServerProtocol.writeGroup(URI, jsonString, new WebSocketChannelMatcher((channel, httpRequestWrapper) -> {
            String type = httpRequestWrapper.getQueryParam("type").orElse("");
            String uid = httpRequestWrapper.getQueryParam("uid").orElse("");
            return (DEVICE_ALL.equals(uid) || (Objects.equals(uid, deviceUid))) && type.contains(typeData);
        }));
    }
}
