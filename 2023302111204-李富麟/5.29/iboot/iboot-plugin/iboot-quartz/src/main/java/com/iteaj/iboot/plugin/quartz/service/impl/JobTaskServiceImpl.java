package com.iteaj.iboot.plugin.quartz.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.iboot.plugin.quartz.entity.JobTask;
import com.iteaj.iboot.plugin.quartz.jobs.SpringSupportJob;
import com.iteaj.iboot.plugin.quartz.mapper.JobTaskMapper;
import com.iteaj.iboot.plugin.quartz.scheduler.SchedulerManager;
import com.iteaj.iboot.plugin.quartz.service.IJobTaskService;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;

@Service
public class JobTaskServiceImpl extends BaseServiceImpl<JobTaskMapper, JobTask> implements IJobTaskService {

    private final SchedulerManager schedulerManager;

    public JobTaskServiceImpl(@Qualifier("defaultSchedulerManager") SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @Override
    @Transactional
    public BooleanResult saveOrUpdate(JobTask entity) {
        JobDataMap dataMap = getJobDataMap(entity);
        if(entity.getId() != null) {
            schedulerManager.updateJob(entity.getJobName(), entity.getConcurrent(), dataMap, entity.getRemark());
        } else {
            String jobName = getJobName(entity);
            entity.setJobName(jobName);
            schedulerManager.addJob(jobName, entity.getConcurrent(), entity.getCron(), dataMap, entity.getRemark());
        }

        return super.saveOrUpdate(entity);
    }

    private JobDataMap getJobDataMap(JobTask entity) {
        String args = entity.getParams();
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(SpringSupportJob.JOB_EXPRESSION, entity.getMethod());
        try {
            if(!StringUtils.hasText(args)) {
                return dataMap;
            } else if(args.startsWith("'") || args.startsWith("\"")) {
                dataMap.put("params", args.substring(1, args.length() - 1));
            } else if(args.startsWith("{")) {
                dataMap.put("params", JSONObject.parseObject(args));
            } else if(args.startsWith("[")) {
                dataMap.put("params", JSONObject.parseArray(args));
            } else {
                dataMap.put("params", args);
            }
        } catch (Exception e) {
            throw new ServiceException("参数格式化异常：" + args);
        }

        return dataMap;
    }

    private String getJobName(JobTask entity) {
        return UUID.fastUUID().toString(false);
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isEmpty(idList)) {
            return BooleanResult.buildFalse("请指定要删除的作业");
        }

        this.listByIds(idList).stream().forEach(item -> {
            schedulerManager.removeJob(item.getJobName());
        });

        return super.removeByIds(idList);
    }
}
