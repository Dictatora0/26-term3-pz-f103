package com.iteaj.iboot.plugin.quartz;

import com.iteaj.framework.spi.admin.Module;
import com.iteaj.iboot.plugin.quartz.jobs.SpringSupportJob;
import com.iteaj.iboot.plugin.quartz.scheduler.SchedulerManager;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

/**
 * create time: 2020/7/18
 *  默认定时任务配置
 * @author iteaj
 * @since 1.0
 */
@EnableScheduling
@ComponentScan({"com.iteaj.iboot.plugin.quartz"})
@MapperScan({"com.iteaj.iboot.plugin.quartz.mapper"})
public class QuartzAutoConfiguration implements BeanFactoryPostProcessor {

    private final SpringSupportJob supportJob = new SpringSupportJob();
    private final static String DEFAULT_JOB_GROUP_NAME = "JOB:GROUP:IBOOT";
    private final static String DEFAULT_TRIGGER_GROUP_NAME = "TRIGGER:GROUP:IBOOT";

    @Bean
    public Module quartzModule() {
        return Module.module("quartz", "任务调度模块", 77777);
    }

    @Bean("schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, DataSource dataSource){

        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();

        // 去除默认的JobFactory, 使用可注入Bean的JobFactory
        factoryBean.setJobFactory(jobFactory);

        // 设置Quartz相关配置
        factoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));

        // 不自动启用
//        factoryBean.setAutoStartup(false);

        // 延时20秒启动
        factoryBean.setStartupDelay(20);

        // 使用数据库存储时需要用到的数据源
        factoryBean.setDataSource(dataSource);
        return factoryBean;
    }

    @Bean("scheduler")
    public Scheduler sysScheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }

    @Bean("defaultSchedulerManager")
    public SchedulerManager defaultSchedulerManager(Scheduler scheduler) {
        return new SchedulerManager(scheduler, DEFAULT_JOB_GROUP_NAME, DEFAULT_TRIGGER_GROUP_NAME);
    }

    @Bean
    public JobFactory jobFactory() {
        return supportJob;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.addBeanPostProcessor(supportJob);
    }
}
