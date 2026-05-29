package com.iteaj.iboot.plugin.quartz.jobs;

import org.quartz.Trigger;

import java.util.Date;

/**
 * create time: 2020/7/19
 *
 * @author iteaj
 * @since 1.0
 */
public class JobEntity {

    private String name;

    private String cron;

    private Class jobClass;

    private String groupName;

    private String description;

    private Date nextFireTime;

    private Date previousFireTime;

    private Trigger.TriggerState state;

    public JobEntity(String name, String groupName) {
        this.name = name;
        this.groupName = groupName;
    }

    public JobEntity(String name, String cron, String groupName, String description, Trigger.TriggerState state) {
        this.name = name;
        this.cron = cron;
        this.groupName = groupName;
        this.description = description;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public JobEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getCron() {
        return cron;
    }

    public JobEntity setCron(String cron) {
        this.cron = cron;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public JobEntity setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public JobEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public Trigger.TriggerState getState() {
        return state;
    }

    public JobEntity setState(Trigger.TriggerState state) {
        this.state = state;
        return this;
    }

    public Class getJobClass() {
        return jobClass;
    }

    public JobEntity setJobClass(Class jobClass) {
        this.jobClass = jobClass;
        return this;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public JobEntity setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
        return this;
    }

    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    public JobEntity setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
        return this;
    }
}
