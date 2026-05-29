package com.iteaj.iboot.module.iot.collect;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.iboot.module.iot.collect.model.EventGroupCollectListener;
import com.iteaj.iboot.module.iot.collect.model.ModelAttrListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollectListenerManager {

    private List<CollectDataListener> listeners = new ArrayList<>();
    private List<SignalCollectListener> signalCollectListeners = new ArrayList<>();
    private List<EventGroupCollectListener> groupCollectListeners = new ArrayList<>();

    private static CollectListenerManager instance = new CollectListenerManager();

    protected CollectListenerManager() { }

    public static CollectListenerManager build(List<CollectDataListener> listeners) {
        if(CollectionUtil.isNotEmpty(listeners)) {
            listeners.forEach(item -> {
                instance.register(item);
            });
        }

        return instance;
    }

    public void register(CollectDataListener listener) {
        if(listener instanceof EventGroupCollectListener) {
            instance.register((EventGroupCollectListener) listener);
        }

        if(listener instanceof SignalCollectListener) {
            instance.register((SignalCollectListener) listener);
        }

        listeners.add(listener);
    }

    public void register(EventGroupCollectListener listener) {
        groupCollectListeners.add(listener);
    }

    public void register(SignalCollectListener listener) {
        signalCollectListeners.add(listener);
    }

    /**
     * 事件发布
     * @param consumer
     */
    public void publish(Consumer<CollectDataListener> consumer) {
        try {
            this.listeners.forEach(consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 模型属性发布
     * @param consumer
     */
    public void modelAttrPublish(Consumer<ModelAttrListener> consumer) {
        try {
            this.listeners.forEach(item -> {
                if(item instanceof ModelAttrListener) {
                    consumer.accept((ModelAttrListener) item);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布设备状态事件
     * @param consumer
     */
    public void deviceStatusPublish(Consumer<DeviceStatusListener> consumer) {
        try {
            this.listeners.forEach(item -> {
                if(item instanceof DeviceStatusListener) {
                    consumer.accept((DeviceStatusListener) item);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 事件源采集发布
     * @param consumer
     */
    public void eventGroupPublish(Consumer<EventGroupCollectListener> consumer) {
        try {
            groupCollectListeners.forEach(consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点位数据采集发布
     * @param consumer
     */
    public void signalPublish(Consumer<SignalCollectListener> consumer) {
        try {
            signalCollectListeners.forEach(consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CollectListenerManager getInstance() {
        return instance;
    }
}
