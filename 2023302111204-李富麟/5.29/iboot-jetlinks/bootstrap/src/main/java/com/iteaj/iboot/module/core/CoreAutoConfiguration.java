package com.iteaj.iboot.module.core;

import cn.afterturn.easypoi.handler.inter.IExcelDictHandler;
import com.iteaj.framework.autoconfigure.CaptchaAutoConfiguration;
import com.iteaj.framework.autoconfigure.FrameworkWebConfiguration;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import com.iteaj.framework.spi.admin.Module;
import com.iteaj.framework.spi.admin.ResourceManager;
import com.iteaj.framework.spi.excel.ExcelDictHandle;
import com.iteaj.framework.spi.excel.IExcelService;
import com.iteaj.iboot.module.core.config.listener.OnlineUserListener;
import com.iteaj.iboot.module.core.exception.AsyncExceptionHandler;
import com.iteaj.iboot.module.core.listener.LoggerListener;
import com.iteaj.iboot.module.core.service.impl.SysResourceManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * create time: 2020/4/25
 *  系统核心模块
 * @author iteaj
 * @since 1.0
 */
@EnableAsync(proxyTargetClass = true)
@ImportAutoConfiguration({
        CaptchaAutoConfiguration.class, // 校验码配置
        FrameworkWebConfiguration.class // 框架Web容器配置
})
@ComponentScan({"com.iteaj.iboot.module.core"})
@MapperScan({"com.iteaj.iboot.module.core.mapper"})
@EnableConfigurationProperties(CoreProperties.class)
public class CoreAutoConfiguration implements WebMvcConfigurer, AsyncConfigurer {

    private final CoreProperties coreProperties;

    public CoreAutoConfiguration(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Bean
    public ResourceManager menuResourceManager() {
        return new SysResourceManager();
    }

    /**
     * 日志监听
     * @return
     */
    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }

    @Bean
    public OnlineUserListener onlineUserListener() {
        return new OnlineUserListener();
    }

    /**
     * 注册系统模块
     * @return
     */
    @Bean
    public Module sysModule() {
        return Module.module("sys", "系统模块", 0, "core");
    }

    /**
     * @return
     */
    @Bean
    @Order(value = 18)
    public OrderFilterChainDefinition coreFilterChainDefinition() {
        return new OrderFilterChainDefinition()
                .addAnon("/core/login", "/js/**", "/css/**", "/img/**"
                        , "/libs/**", "/doc/**", "/favicon.ico");
    }

    @Bean
    @Order(value = 9999)
    public OrderFilterChainDefinition authFilterChainDefinition() {
        return new OrderFilterChainDefinition().addInclude("/**");
    }

    @Bean
    public IExcelDictHandler excelDictHandler(IExcelService excelService) {
        return new ExcelDictHandle(excelService);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
}
