package com.iteaj.framework.spring.condition;

import com.iteaj.framework.autoconfigure.FrameworkProperties;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create time: 2021/3/21
 *  集群条件
 * @see FrameworkProperties#isCluster() 在配置文件启用
 * @author iteaj
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ClusterConditionalImpl.class)
public @interface ConditionalOnCluster {

}
