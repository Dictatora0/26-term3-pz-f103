package com.iteaj.iboot.plugin.quartz.jobs;

import lombok.Data;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Data
public class QuartzJobContext {

    private Object bean;
    private Method method;
    private static ExpressionParser parser=new SpelExpressionParser();

    public QuartzJobContext(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object invoke(String expression, JobExecutionContext jobExecutionContext) {
        final JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        if(parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if(JobDataMap.class.isAssignableFrom(parameterTypes[i])) {
                    params[i] = jobDataMap;
                } else if(JobExecutionContext.class.isAssignableFrom(parameterTypes[i])) {
                    params[i] = jobExecutionContext;
                } else {
                    params[i] = null;
                }
            }
        }

        return ReflectionUtils.invokeMethod(method, bean, params);
    }

}
