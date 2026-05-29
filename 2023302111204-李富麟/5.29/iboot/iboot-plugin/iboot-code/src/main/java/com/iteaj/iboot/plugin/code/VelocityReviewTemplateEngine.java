package com.iteaj.iboot.plugin.code;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.iteaj.iboot.plugin.code.dto.ReviewDto;
import com.iteaj.iboot.plugin.code.vue.VueInjectionConfig;
import com.iteaj.iboot.plugin.code.utils.LowCodeUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

public class VelocityReviewTemplateEngine extends VelocityTemplateEngine {

    private VelocityEngine velocityEngine;
    private LowCodeProperties properties;
    private ReviewDto reviewDto = new ReviewDto();

    public VelocityReviewTemplateEngine(LowCodeProperties properties) {
        this.properties = properties;
    }

    @Override
    public VelocityTemplateEngine init(ConfigBuilder configBuilder) {
        if (null == velocityEngine) {
            Properties p = new Properties();
            p.setProperty(ConstVal.VM_LOAD_PATH_KEY, ConstVal.VM_LOAD_PATH_VALUE);
            p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, StringPool.EMPTY);
            p.setProperty(Velocity.ENCODING_DEFAULT, ConstVal.UTF8);
            p.setProperty(Velocity.INPUT_ENCODING, ConstVal.UTF8);
            p.setProperty("file.resource.loader.unicode", StringPool.TRUE);
            velocityEngine = new VelocityEngine(p);
        }

        this.setConfigBuilder(configBuilder);
        configBuilder.setInjectionConfig(new VueInjectionConfig(configBuilder, this.properties) {

            @Override
            public void initTableMap(TableInfo tableInfo) {
                super.initTableMap(tableInfo);
                String serviceName = tableInfo.getServiceName();

                this.getMap().put("moduleName", LowCodeUtil.getModuleName(tableInfo.getComment()));
                this.getMap().put("serviceName", LowCodeUtil.firstLowerServiceName(serviceName));
            }

            @Override
            public IFileCreate getFileCreate() {
                return (configBuilder, fileType, filePath) -> true;
            }
        });

        return this;
    }

    @Override
    public void writer(Map<String, Object> objectMap, String templatePath, String outputFile) throws Exception {
        Template template = this.velocityEngine.getTemplate(templatePath, ConstVal.UTF8);
        TableInfo tableInfo = (TableInfo) objectMap.get("table");
        try (StringWriter sw = new StringWriter()) {
            template.merge(new VelocityContext(objectMap), sw);
            if(outputFile.endsWith(".vue")) {
                this.reviewDto.setHtml(sw.toString());
            } else if(outputFile.contains(tableInfo.getXmlName())
                    && outputFile.endsWith(".xml")) {
                this.reviewDto.setMapperXml(sw.toString());
            } else if(outputFile.contains(tableInfo.getMapperName())
                    && outputFile.endsWith(".java")) {
                this.reviewDto.setMapper(sw.toString());
            } else if(outputFile.contains(tableInfo.getServiceName())) {
                this.reviewDto.setService(sw.toString());
            } else if(outputFile.contains(tableInfo.getServiceImplName())) {
                this.reviewDto.setServiceImpl(sw.toString());
            } else if(outputFile.contains(tableInfo.getControllerName())) {
                this.reviewDto.setController(sw.toString());
            } else if(outputFile.contains(tableInfo.getEntityName())) {
                this.reviewDto.setEntity(sw.toString());
            }
        }
    }

    public ReviewDto getReviewDto() {
        return reviewDto;
    }

    public void setReviewDto(ReviewDto reviewDto) {
        this.reviewDto = reviewDto;
    }
}
