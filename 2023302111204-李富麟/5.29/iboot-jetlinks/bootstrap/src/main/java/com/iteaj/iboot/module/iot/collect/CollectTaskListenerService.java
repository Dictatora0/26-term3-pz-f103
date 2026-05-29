package com.iteaj.iboot.module.iot.collect;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.event.ApplicationReadyListener;
import com.iteaj.iboot.module.iot.IBootThreadManger;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.collect.store.StoreActionFactory;
import com.iteaj.iboot.module.iot.dto.CollectTaskDto;
import com.iteaj.iboot.module.iot.entity.CollectTask;
import com.iteaj.iboot.module.iot.service.ICollectTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 数据采集服务
 */
@Service
public class CollectTaskListenerService implements ApplicationReadyListener {

    private final IBootThreadManger threadManger;
    private final IotCacheManager cacheManager;
    private final StoreActionFactory storeActionFactory;
    private final ICollectTaskService collectTaskService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<Long, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

    public CollectTaskListenerService(IBootThreadManger threadManger, IotCacheManager cacheManager
            , StoreActionFactory storeActionFactory, ICollectTaskService collectTaskService) {
        this.threadManger = threadManger;
        this.cacheManager = cacheManager;
        this.storeActionFactory = storeActionFactory;
        this.collectTaskService = collectTaskService;
    }

    public void statusSwitch(Long id, String status) throws CollectException{

        if(status.equals("run")) {
            CollectTaskDto taskDto = collectTaskService.collectDetailById(id);

            if(taskDto == null) {
                throw new ServiceException("任务不存在");
            }

            if(taskDto.getStatus().equals(status)) {
                throw new ServiceException("状态切换异常");
            }

            ScheduledFuture future = scheduledFutureMap.get(id);
            Object result = validateTask(taskDto);// 校验采集任务
            if(result instanceof String) { // 任务校验失败
                throw new CollectException((String) result);
            }

            if(future == null) {
                createScheduledTask(taskDto);
            } else if(future.isCancelled()) {
                scheduledFutureMap.remove(id);
                createScheduledTask(taskDto);
            }
        } else if(status.equals("stop")){
            ScheduledFuture future = scheduledFutureMap.remove(id);
            if(future != null && !future.isCancelled()) {
                future.cancel(true);
            }
        } else {
            throw new IllegalArgumentException("不支持的状态["+status+"]");
        }

        collectTaskService.update(Wrappers.<CollectTask>lambdaUpdate()
                .set(CollectTask::getStatus, status)
                .set(CollectTask::getReason, "")
                .set(CollectTask::getUpdateTime, new Date())
                .eq(CollectTask::getId, id));
    }

    private Object validateTask(CollectTaskDto taskDto) {
        try {
            if(CollectionUtils.isEmpty(taskDto.getDetails())) {
                return "未找到任务配置记录";
            }

            if(!StringUtils.hasText(taskDto.getCron())) {
                collectTaskService.update(Wrappers.<CollectTask>lambdaUpdate()
                        .set(CollectTask::getStatus, "fail")
                        .set(CollectTask::getReason, "没有配置cron")
                        .eq(CollectTask::getId, taskDto.getId()));

                return "没有配置cron";
            }

            taskDto.getDetails().forEach(item -> {
                String reason = null;

                if(CollectionUtils.isEmpty(item.getDevices())) {
                    reason = "任务详情["+item.getPointGroupName()+"]没有绑定任何设备";
                }

                if(reason != null) {
                    collectTaskService.update(Wrappers.<CollectTask>lambdaUpdate()
                            .set(CollectTask::getStatus, "fail")
                            .set(CollectTask::getReason, reason)
                            .set(CollectTask::getUpdateTime, null)
                            .eq(CollectTask::getId, taskDto.getId()));

                    throw new CollectException(reason);
                }
            });
        } catch (CollectException e) {
            logger.error("校验任务失败 {} - 任务:{}", e.getMessage(), taskDto.getName());
            return e.getMessage();
        } catch (Exception e) {
            logger.error("校验任务失败 {} - 任务:{}", e.getMessage(), taskDto.getName(), e);
            return e.getMessage();
        }

        return taskDto;
    }

    private void createScheduledTask(CollectTaskDto taskDto) {
        ThreadPoolTaskScheduler scheduler = threadManger.getScheduler();
        scheduledFutureMap.put(taskDto.getId(), scheduler.schedule(new CollectActionTask(taskDto, cacheManager), new CronTrigger(taskDto.getCron())));
    }

    @Override
    public void started(ApplicationContext context) {
        collectTaskService.runningCollectTaskDetail()
                .stream().filter(item -> item != null && !CollectionUtils.isEmpty(item.getDetails()))
                .map(item -> validateTask(item))
                .filter(item -> item instanceof CollectTaskDto) // 校验之后重新过滤
                .forEach(item -> createScheduledTask((CollectTaskDto) item));
    }
}
