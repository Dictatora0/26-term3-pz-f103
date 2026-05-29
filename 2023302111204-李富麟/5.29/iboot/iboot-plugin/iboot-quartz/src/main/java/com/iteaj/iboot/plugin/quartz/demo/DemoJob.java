package com.iteaj.iboot.plugin.quartz.demo;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create time: 2020/7/18
 *
 * @author iteaj
 * @since 1.0
 */
public class DemoJob implements Job {

    private Logger logger = LoggerFactory.getLogger(DemoJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap args = jobExecutionContext.getMergedJobDataMap();
//        jobExecutionContex
        logger.info("测试Job执行, 参数: demo={}, author={}", args.get("demo"), args.get("author"));
    }

    public String test(JobExecutionContext context, String name, int k) {
        return name + k;
    }
}
