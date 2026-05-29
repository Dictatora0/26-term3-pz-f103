package com.iteaj.framework.plugin;

public interface Plugin {

    /**
     * 插件名称
     * @return
     */
    String getPluginName();

    /**
     * 插件说明
     * @return
     */
    String getPluginDesc();

    /**
     * 打印banner
     */
    default void banner() {

    }
}
