package com.iteaj.iboot.plugin.quartz.demo;

import com.iteaj.iboot.plugin.quartz.jobs.QuartzJob;
import com.iteaj.iboot.plugin.quartz.jobs.QuartzTask;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuartzTask(desc = "测试任务")
public class QuartzJobTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @QuartzJob("测试作业")
    public void job(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap dataMap = context.getMergedJobDataMap();
        logger.info("调度任务测试用例执行... 参数：{}", dataMap.get("params"));
    }
}
