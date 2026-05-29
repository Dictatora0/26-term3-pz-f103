package com.iteaj.iboot.plugin.code.design;

import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.iteaj.iboot.plugin.code.LowCodeConst;
import com.iteaj.iboot.plugin.code.gen.DesignConfigBuilder;

import java.io.File;

public class DtoFileOutConfig extends FileOutConfig {

    private DesignConfigBuilder builder;

    public DtoFileOutConfig(DesignConfigBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String getTemplatePath() {
        return "/templates/lcd.dto.java.vm";
    }

    @Override
    public String outputFile(TableInfo tableInfo) {
        String dto = builder.getPathInfo().get(LowCodeConst.DTO_PATH);
        return dto + File.separator + tableInfo.getEntityName()+"Dto.java";
    }
}
