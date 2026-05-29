package com.iteaj.iboot.module.iot.collect.model;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.spi.event.ApplicationReadyListener;
import com.iteaj.iboot.module.iot.IBootThreadManger;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.EventSourceDto;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.iboot.module.iot.service.IDeviceGroupService;
import com.iteaj.iboot.module.iot.service.IEventSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class EventSourceCollectService implements ApplicationReadyListener {

    private final IotCacheManager cacheManager;
    private final IBootThreadManger threadManger;
    private final IDeviceGroupService deviceGroupService;
    private final IEventSourceService eventSourceService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<Long, ScheduledFuture> futures = new ConcurrentHashMap<>(128);

    public EventSourceCollectService(IotCacheManager cacheManager
            , IBootThreadManger threadManger, IDeviceGroupService deviceGroupService
            , IEventSourceService eventSourceService) {
        this.cacheManager = cacheManager;
        this.threadManger = threadManger;
        this.deviceGroupService = deviceGroupService;
        this.eventSourceService = eventSourceService;
    }

    @Override
    public void started(ApplicationContext context) {
        this.eventSourceService.list(Wrappers.<EventSource>lambdaQuery()
                .eq(EventSource::getStatus, FuncStatus.enabled)).forEach(item -> {

            try {
                EventSourceDto source = eventSourceService.collectDetailById(item.getId())
                        .ifNotPresentThrow("事件源不存在").getData();

                eventSourceService.listProductById(source.getId(), null).forEach(product -> {
                    if(product.getStatus() == FuncStatus.disabled) {
                        throw new ServiceException("产品["+product.getName()+"]未启用");
                    }
                });

                ThreadPoolTaskScheduler scheduler = threadManger.getScheduler();
                ScheduledFuture<?> schedule = scheduler.schedule(new EventSourceTask(scheduler
                        , source, cacheManager), new CronTrigger(source.getCron()));
                futures.put(source.getId(), schedule);
            } catch (ServiceException e) {
                logger.error("启动事件源失败({}) {}", item.getName(), e.getMessage());
                eventSourceService.update(Wrappers.<EventSource>lambdaUpdate()
                        .set(EventSource::getReason, e.getMessage())
                        .eq(EventSource::getId, item.getId()));
            } catch (Exception e) {
                logger.error("启动事件源失败({}) {}", item.getName(), e.getMessage(), e);
                eventSourceService.update(Wrappers.<EventSource>lambdaUpdate()
                        .set(EventSource::getReason, "未知错误")
                        .eq(EventSource::getId, item.getId()));
            }
        });
    }

    public BooleanResult switchTask(Long id, FuncStatus status) {
        if(status == null) {
            return BooleanResult.buildFalse("未指定切换状态");
        }
        if(id == null) {
            return BooleanResult.buildFalse("未指定事件源id");
        }

        EventSourceDto source = eventSourceService.collectDetailById(id)
                .ifNotPresentThrow("事件源不存在").getData();

        if(StrUtil.isBlank(source.getCron())) {
            return BooleanResult.buildFalse("cron表达式错误");
        }

        ScheduledFuture scheduledFuture = futures.get(id);
        if(status == FuncStatus.enabled) { // 启用
            this.eventSourceService.listProductById(source.getId(), null).forEach(product -> {
                if(product.getStatus() == FuncStatus.disabled) {
                    throw new ServiceException("产品["+product.getName()+"]未启用");
                }
            });

            if(scheduledFuture != null) { // 已经存在则先移除并取消
                futures.remove(id).cancel(true);
            }

            ThreadPoolTaskScheduler scheduler = threadManger.getScheduler();
            ScheduledFuture<?> schedule = scheduler.schedule(new EventSourceTask(scheduler
                    , source, cacheManager), new CronTrigger(source.getCron()));
            futures.put(id, schedule);
        } else { // 禁用
            // 取消定时任务
            if(scheduledFuture != null) {
                futures.remove(id).cancel(true);
            }
        }

        return this.eventSourceService.update(Wrappers.<EventSource>lambdaUpdate()
                .set(EventSource::getStatus, status)
                .set(EventSource::getReason, "")
                .eq(EventSource::getId, id));
    }
}
