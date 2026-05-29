package com.iteaj.iboot.plugin.quartz.jobs;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

/**
 * create time: 2020/7/18
 *
 * @author iteaj
 * @since 1.0
 */
public class AutowireJobFactory extends AdaptableJobFactory {

    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {

        Object jobInstance = super.createJobInstance(bundle);

        //对Job对象注入候选对象
        capableBeanFactory.autowireBean(jobInstance);

        return jobInstance;
    }
}
