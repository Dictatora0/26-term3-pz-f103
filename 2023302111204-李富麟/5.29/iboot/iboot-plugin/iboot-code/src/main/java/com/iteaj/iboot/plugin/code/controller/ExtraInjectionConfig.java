package com.iteaj.iboot.plugin.code.controller;

import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.iteaj.iboot.plugin.code.utils.LowCodeUtil;

import java.util.HashMap;

public class ExtraInjectionConfig extends InjectionConfig {

    @Override
    public void initMap() {
        this.setMap(new HashMap<>());
    }

    @Override
    public void initTableMap(TableInfo tableInfo) {
        this.initMap();
        String serviceName = tableInfo.getServiceName();

        this.getMap().put("moduleName", LowCodeUtil.getModuleName(tableInfo.getComment()));
        this.getMap().put("serviceName", LowCodeUtil.firstLowerServiceName(serviceName));
    }
}
