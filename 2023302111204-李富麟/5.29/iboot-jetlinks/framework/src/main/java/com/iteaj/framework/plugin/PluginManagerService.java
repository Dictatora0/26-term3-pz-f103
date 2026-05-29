package com.iteaj.framework.plugin;

import cn.hutool.core.collection.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PluginManagerService implements ApplicationListener<ApplicationStartedEvent> {

    private final List<Plugin> plugins;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public PluginManagerService(List<Plugin> plugins) {
        this.plugins = plugins != null ? plugins : new ArrayList<>();
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        String plugins = this.plugins.stream()
                .map(item -> item.getPluginName())
                .collect(Collectors.joining("、"));
        logger.info("框架已加载完成所有插件[{}]", plugins);
//        System.out.println("------------------------------------------------- 已启用的插件 -----------------------------------------------");
        this.plugins.forEach(item -> {
            item.banner();
        });
//        System.out.println("------------------------------------------------------------------------------------------------------------");
    }
}
