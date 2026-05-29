package com.iteaj.iboot.plugin.code.design;

import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.iteaj.iboot.plugin.code.LowCodeProperties;
import com.iteaj.iboot.plugin.code.gen.DesignConfigBuilder;
import com.iteaj.iboot.plugin.code.gen.DesignTableInfo;
import com.iteaj.iboot.plugin.code.utils.LowCodeUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LcdVueInjectionConfig extends LcdInjectionConfig {

    private LowCodeProperties properties;
    public LcdVueInjectionConfig(DesignConfigBuilder builder, LowCodeProperties properties) {
        setConfig(builder);
        this.properties = properties;
    }

    @Override
    public void initMap() {
        this.setMap(new HashMap<>());
    }

    @Override
    public void initTableMap(TableInfo tableInfo) {
        this.setMap(new HashMap<>()); // 清空上一张表信息的所有配置
        final DesignTableInfo info = (DesignTableInfo) tableInfo;
        getMap().put("formFields", info.getFormFields());
        getMap().put("searchFields", info.getSearchFields());
        getMap().put("tableFields", info.getFormTableFields());
        getMap().put("moduleName", LowCodeUtil.getModuleName(tableInfo.getComment()));

        String serviceName = tableInfo.getServiceName();
        this.getMap().put("serviceName", LowCodeUtil.firstLowerServiceName(serviceName));
    }

    @Override
    public List<FileOutConfig> getFileOutConfigList() {
        return Arrays.asList(new FileOutConfig() {

            @Override
            public String getTemplatePath() {
                return "/templates/lcd.ivzone.vue.vm";
            }

            @Override
            public String outputFile(TableInfo tableInfo) {
                String moduleName = getConfig().getPackageInfo().get(ConstVal.MODULE_NAME);
                String entityName = tableInfo.getEntityPath();
                return moduleName + File.separator + entityName + File.separator + "index.vue";
            }
        });
    }

    @Override
    public ILcdFileCreate getFileCreate() {
        return (configBuilder, fileType, filePath) -> {
            // 只生成前端代码
            if(fileType != FileType.OTHER) {
                return false;
            } else {
                // 只生成.vue结尾的文件
                return filePath.endsWith(".vue");
            }
        };
    }
}
