package com.iteaj.iboot.plugin.quartz.scheduler;

import com.iteaj.iboot.plugin.quartz.jobs.SpringSupportJob;
import org.quartz.*;

/**
 * create time: 2020/7/19
 *
 * @author iteaj
 * @since 1.0
 */
public class SchedulerManager {

    private final Scheduler scheduler;
    private final String JOB_GROUP_NAME;
    private final String TRIGGER_GROUP_NAME;

    public SchedulerManager(Scheduler scheduler, String JOB_GROUP_NAME, String TRIGGER_GROUP_NAME) {
        this.scheduler = scheduler;
        this.JOB_GROUP_NAME = JOB_GROUP_NAME;
        this.TRIGGER_GROUP_NAME = TRIGGER_GROUP_NAME;
    }

    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     * @param jobName 任务名
     * @param cls 任务
     */
    public void addJob(String jobName, Class<? extends Job> cls, String cron) {
        addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, cls, cron, null, null);
    }

    public void addJob(String jobName, Class<? extends Job> cls, String cron, String desc) {
        addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, cls, cron, null, desc);
    }

    /**
     * @param jobName 作业名称
     * @param isConcurrent 是否不允许并发执行(对于同一个作业)
     * @param cron
     * @param map
     * @param desc
     */
    public void addJob(String jobName, boolean isConcurrent, String cron, JobDataMap map, String desc) {
        if(!isConcurrent) {
            addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, SpringSupportJob.NON_CONCURRENT_EMPTY_JOB, cron, map, desc);
        } else {
            addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, SpringSupportJob.EMPTY_JOB, cron, map, desc);
        }
    }

    public void addJob(String jobName, Class<? extends Job> jobClass, String cron, JobDataMap map, String desc) {
        addJob(jobName, JOB_GROUP_NAME, jobName, TRIGGER_GROUP_NAME, jobClass, cron, map, desc);
    }

    protected void addJob(String jobName, String jobGroupName, String triggerName
            , String triggerGroupName, Class<? extends Job> jobClass, String cron, JobDataMap map, String desc) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if(jobDetail != null) throw new RuntimeException("作业已经存在[" + jobKey + "]");

            map = map == null ? new JobDataMap() : map;
            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName
                    , jobGroupName).usingJobData(map).withDescription(desc).build();

            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);

            // 按新的cronExpression表达式构建一个新的trigger
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
                    .withSchedule(scheduleBuilder).build();

            // 交给scheduler去调度
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新作业
     * @param jobName
     * @param isConCurrent 是否并发  使用默认作业{@link SpringSupportJob} <hr>
     *     {@link SpringSupportJob.EmptyJob}
     *     {@link SpringSupportJob.NonConcurrentEmptyJob}
     */
    public void updateJob(String jobName, boolean isConCurrent, JobDataMap map, String desc) {
        Class jobClass = isConCurrent ? SpringSupportJob.EMPTY_JOB : SpringSupportJob.NON_CONCURRENT_EMPTY_JOB;
        this.updateJob(jobName, jobClass, map, desc);
    }

    /**
     * 更新作业
     * @param jobName 作业名称
     * @param jobClass 作业
     * @param map 作业数据
     */
    public void updateJob(String jobName, Class<? extends Job> jobClass, JobDataMap map, String desc) {

        try {
            final JobKey jobKey = new JobKey(jobName, JOB_GROUP_NAME);
            final JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) throw new RuntimeException("作业不存在[" + jobName + "]");

            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, JOB_GROUP_NAME)
                    .usingJobData(map).storeDurably().withDescription(desc).build();

            // 覆盖掉原先的作业信息
            scheduler.addJob(job, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改一个任务的触发时间
     * @param triggerName
     */
    public void updateTrigger(String triggerName, String cron) {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP_NAME);
        try {

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null)  throw new RuntimeException("触发器不存在[" + triggerKey + "]");

            String oldTime = trigger.getCronExpression();

            if (!oldTime.equalsIgnoreCase(cron)) {
                // trigger已存在，则更新相应的定时设置
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);

                // 按新的cronExpression表达式重新构建trigger
                CronTrigger newTrigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                        .withSchedule(scheduleBuilder).build();

                // 按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, newTrigger);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 移除一个任务<hr>
     *     作业名和触发器名称相同
     * @param jobName
     */
    public void removeJob(String jobName) {
        this.removeJob(jobName, jobName);
    }

    /**
     * 移除一个任务
     * @param jobName
     * @param triggerName
     */
    public void removeJob(String jobName, String triggerName) {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP_NAME);
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
        try {

            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger == null) return;

//            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(jobKey);// 删除任务
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String jobName) {
        try {
            return scheduler.getJobDetail(JobKey.jobKey(jobName, JOB_GROUP_NAME)) != null;
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 暂停一个作业
     * @param jobName
     */
    public void pauseJob(String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP_NAME);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 恢复一个任务(使用默认组名)
     * @param jobName
     */
    public void resumeJob(String jobName) {
        resumeJob(jobName, JOB_GROUP_NAME);
    }

    /**
     * 恢复一个任务
     * @param jobName
     * @param jobGroupName
     */
    protected void resumeJob(String jobName, String jobGroupName) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @Description 关闭所有定时任务
     */
    public void shutdownJobs() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
	    } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 立即运行任务，只会运行一次。
     * @param jobName
     */
    public void fireJob(String jobName) {
        fireJob(jobName, JOB_GROUP_NAME, null);
    }

    /**
     * 立即运行一次, 带参数
     * @param jobName
     * @param data
     */
    public void fireJob(String jobName, JobDataMap data) {
        fireJob(jobName, JOB_GROUP_NAME, data);
    }

    /**
     * 立即运行任务，只会运行一次。
     * @param jobName
     * @param jobGroupName
     */
    protected void fireJob(String jobName, String jobGroupName, JobDataMap data) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
        try {
            scheduler.triggerJob(jobKey, data);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取任务状态
     * 		NONE: 不存在
     * 		NORMAL: 正常
     * 		PAUSED: 暂停
     * 		COMPLETE:完成
     * 		ERROR : 错误
     * 		BLOCKED : 阻塞
     * @param jobName 触发器名, 默认作业名
     */
    public Trigger.TriggerState getTriggerState(String jobName){
        return getTriggerState(jobName, TRIGGER_GROUP_NAME);
    }

    protected Trigger.TriggerState getTriggerState(String triggerName, String triggerGroup){
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        try {
            return scheduler.getTriggerState(triggerKey);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Scheduler getSysScheduler() {
        return scheduler;
    }
}
