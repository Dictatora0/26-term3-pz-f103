package com.iteaj.iboot.plugin.code.vue;

import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.iteaj.iboot.plugin.code.LowCodeProperties;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 前端vue模板
 */
public class VelocityVueTemplateEngine extends VelocityTemplateEngine {

    private LowCodeProperties properties;
    public VelocityVueTemplateEngine(LowCodeProperties properties) {
        this.properties = properties;
    }

    @Override
    public VelocityTemplateEngine init(ConfigBuilder configBuilder) {
        return super.init(configBuilder.setInjectionConfig(new VueInjectionConfig(configBuilder, this.properties) {
            @Override
            public List<FileOutConfig> getFileOutConfigList() {
                return Arrays.asList(new FileOutConfig() {

                    @Override
                    public String getTemplatePath() {
                        return "/templates/ivzone.vue.vm";
                    }

                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        String outputDir = getConfigBuilder().getGlobalConfig().getOutputDir();
                        String moduleName = getConfig().getPackageInfo().get(ConstVal.MODULE_NAME);
                        String entityName = tableInfo.getEntityPath();
                        return outputDir + "/" + moduleName + "/" + entityName + "/index.vue";
                    }
                });
            }

            @Override
            public IFileCreate getFileCreate() {
                return (configBuilder1, fileType, filePath) -> {
                    // 只生成前端代码
                    if (fileType == FileType.OTHER) {
                        // 只生成.vue结尾的文件
                        boolean endsWith = filePath.endsWith(".vue");
                        if (endsWith) {
                            File file = new File(filePath);
                            if (!file.exists()) {
                                file.getParentFile().mkdirs();
                            }

                            return !file.exists() || getConfigBuilder().getGlobalConfig().isFileOverride();
                        }
                    }
                    return false;
                };
            }
        }));
    }
}
