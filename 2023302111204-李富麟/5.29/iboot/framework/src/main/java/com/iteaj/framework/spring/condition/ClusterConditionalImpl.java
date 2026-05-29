package com.iteaj.framework.spring.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * create time: 2021/3/21
 *
 * @author iteaj
 * @since 1.0
 */
public class ClusterConditionalImpl implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty("framework.cluster", Boolean.class, false);
    }
}
