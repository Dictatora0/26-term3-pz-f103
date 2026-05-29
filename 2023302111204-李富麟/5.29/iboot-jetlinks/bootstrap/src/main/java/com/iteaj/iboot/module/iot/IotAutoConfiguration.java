package com.iteaj.iboot.module.iot;

import com.iteaj.framework.spi.admin.Module;
import com.iteaj.framework.spring.condition.ConditionalOnCluster;
import com.iteaj.framework.spring.condition.ConditionalOnRedis;
import com.iteaj.iboot.module.iot.cache.IotCacheManager;
import com.iteaj.iboot.module.iot.cache.IotLocalCacheManager;
import com.iteaj.iboot.module.iot.cache.data.HashMapRealtimeDataService;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataService;
import com.iteaj.iboot.module.iot.cache.data.RedisRealtimeDataService;
import com.iteaj.iboot.module.iot.collect.CollectDataListener;
import com.iteaj.iboot.module.iot.collect.CollectListenerManager;
import com.iteaj.iboot.module.iot.collect.model.DataPersistenceListener;
import com.iteaj.iboot.module.iot.collect.model.DeviceStatusModelApiManager;
import com.iteaj.iboot.module.iot.collect.model.RDBMSPersistenceListener;
import com.iteaj.iboot.module.iot.cache.data.RealtimeDataCacheListener;
import com.iteaj.iboot.module.iot.collect.store.RDBMStoreAction;
import com.iteaj.iboot.module.iot.collect.store.StoreActionFactory;
import com.iteaj.iboot.module.iot.collect.websocket.RealtimePushListener;
import com.iteaj.iot.tools.db.rdbms.DefaultRdbmsSqlManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.List;

@Profile("iot")
@ComponentScan({"com.iteaj.iboot.module.iot"})
@MapperScan({"com.iteaj.iboot.module.iot.mapper"})
@AutoConfigureAfter(JdbcTemplateAutoConfiguration.class)
@EnableConfigurationProperties(IotMsnProperties.class)
@ImportAutoConfiguration(IotProtocolSupplierAutoConfiguration.class)
public class IotAutoConfiguration {

    /**
     * 物联网模块
     * @return
     */
    @Bean
    public Module iotModule() {
        return Module.module("iot", "物联网模块", 55555);
    }

    @Bean
    public RDBMStoreAction rdbmStoreAction() {
        return new RDBMStoreAction();
    }

    @Bean
    public DefaultRdbmsSqlManager rdbmsManager(DataSource dataSource) {
        return new DefaultRdbmsSqlManager(dataSource);
    }

    @Bean
    public StoreActionFactory storeActionFactory() {
        return StoreActionFactory.getInstance();
    }

    @Bean
    public IotCacheManager iotLocalCacheManager() {
        return new IotLocalCacheManager();
    }

    /**
     * 数据持久化处理
     * @param rdbmsSqlManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DataPersistenceListener.class)
    public RDBMSPersistenceListener rdbmsPersistenceListener(DefaultRdbmsSqlManager rdbmsSqlManager) {
        return new RDBMSPersistenceListener(rdbmsSqlManager);
    }

    /**
     * 实时数据缓存处理
     * @param realtimeDataService
     * @return
     */
    @Bean
    public RealtimeDataCacheListener realtimeDataStoreListener(RealtimeDataService realtimeDataService) {
        return new RealtimeDataCacheListener(realtimeDataService);
    }

    @Bean
    public CollectListenerManager eventGroupCollectManager(@Autowired(required = false) List<CollectDataListener> listeners) {
        return CollectListenerManager.build(listeners);
    }

    /**
     * 设备状态采集
     * @return
     */
    @Bean
    public DeviceStatusModelApiManager deviceStatusModelApiManager() {
        return DeviceStatusModelApiManager.getInstance();
    }

    /**
     * redis实时数据
     * @return
     */
    @Bean
    @ConditionalOnRedis
    @ConditionalOnCluster
    public RealtimeDataService redisRealtimeDataService() {
        return new RedisRealtimeDataService();
    }

    /**
     * 本地实时数据
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({RealtimeDataService.class})
    public RealtimeDataService localRealtimeDataService(IotCacheManager cacheManager) {
        return new HashMapRealtimeDataService(cacheManager);
    }

    /**
     * websocket监听实时采集的数据
     * @return
     */
    @Bean
    public RealtimePushListener collectDataRealtimeListener(RealtimeDataService realtimeDataService) {
        return new RealtimePushListener(realtimeDataService);
    }

    @Bean
    public IBootThreadManger iBootThreadManger(IotMsnProperties properties) {
        Integer poolSize = properties.getTask().getPoolSize();
        TaskSchedulerBuilder builder = new TaskSchedulerBuilder(poolSize, false
                , null, properties.getTask().getThreadNamePrefix(), null);
        return new IBootThreadManger(builder.build());
    }
}
