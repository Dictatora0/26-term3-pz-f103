package com.iteaj.iboot.module.iot.collect.model;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;

import java.util.concurrent.Executor;

public class EventSourceTask implements Runnable {

    private Executor executor;
    private EventSourceDto sourceDto;
    private IotCacheManager cacheManager;

    public EventSourceTask(Executor executor, EventSourceDto sourceDto, IotCacheManager cacheManager) {
        this.executor = executor;
        this.sourceDto = sourceDto;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        if(CollectionUtil.isNotEmpty(sourceDto.getGroups())) {
            sourceDto.getGroups().forEach(group -> {
                executor.execute(new CollectGroupTask(group, sourceDto, cacheManager));
            });
        }
    }
}
