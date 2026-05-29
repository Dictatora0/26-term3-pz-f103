package com.iteaj.framework.autoconfigure;

import com.iteaj.framework.plugin.Plugin;
import com.iteaj.framework.plugin.PluginManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.List;

public class PluginAutoConfiguration {

    @Bean
    public PluginManagerService pluginManagerService(@Autowired(required = false) List<Plugin> plugins) {
        return new PluginManagerService(plugins);
    }
}
