package com.iteaj.iboot.plugin.code.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.admin.DictResource;
import com.iteaj.framework.spi.admin.ResourceManager;
import com.iteaj.iboot.plugin.code.LowCodeProperties;
import com.iteaj.iboot.plugin.code.design.*;
import com.iteaj.iboot.plugin.code.dto.GenConfigDto;
import com.iteaj.iboot.plugin.code.dto.ReviewDto;
import com.iteaj.iboot.plugin.code.entity.LcdDesign;
import com.iteaj.iboot.plugin.code.gen.DesignConfigBuilder;
import com.iteaj.iboot.plugin.code.service.ILcdDesignService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 在线低代码表单设计
 */
@RestController
@RequestMapping("/lcd/online")
public class DesignGeneratorController extends BaseController {

    private final ResourceManager resourceManager;
    private final LowCodeProperties properties;
    private final ILcdDesignService lcdDesignService;
    public DesignGeneratorController(ResourceManager resourceManager
            , LowCodeProperties properties, ILcdDesignService lcdDesignService) {
        this.resourceManager = resourceManager;
        this.properties = properties;
        this.lcdDesignService = lcdDesignService;
    }

    /**
     * 获取字典类型列表
     * @return
     */
    @GetMapping("dictType")
    public Result<List<DictResource>> dictType() {
        return success(resourceManager.getDictResources());
    }
    /**
     * 生成后端代码
     * @param dto
     */
    @PostMapping("java")
    public Result<Boolean> generator(@RequestBody GenConfigDto dto) {
        if(org.springframework.util.CollectionUtils.isEmpty(dto.getTables())) {
            return fail("请选择要生成的表");
        }

        GlobalConfig globalConfig = new GlobalConfig();
        BeanUtils.copyProperties(properties.getLgc(), globalConfig);

        // 设置输出目录
        globalConfig.setOutputDir(dto.getOutDir());
        // 文件是否覆盖
        globalConfig.setFileOverride(dto.isFileOverride());
        DesignConfigBuilder configBuilder = new DesignConfigBuilder(properties.getLpc(), properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), globalConfig, dto.getTables());

        // 生成java相关代码
        configBuilder.setInjectionConfig(new LcdDtoInjectionConfig(configBuilder));
        new LcdVelocityTemplateEngine().init(pretreatmentConfigBuilder(configBuilder)).mkdirs().batchOutput().open();

        // 生成菜单
//        genMenu(dto, configBuilder);

        return success(true);
    }

    /**
     * 生成前端代码
     * @param dto
     */
    @PostMapping("vue")
    public Result<byte[]> download(@RequestBody GenConfigDto dto) throws IOException {
        if(!CollectionUtil.isNotEmpty(dto.getTables())) {
            return fail("请选择生成的表");
        }

        DesignConfigBuilder configBuilder = new DesignConfigBuilder(properties.getLpc(), properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), properties.getLgc(), dto.getTables());
        configBuilder.setInjectionConfig(new LcdVueInjectionConfig(configBuilder, properties));

        // 生成文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new LcdVelocityVueTemplateZipEngine(outputStream, properties)
                .init(pretreatmentConfigBuilder(configBuilder)).mkdirs().batchOutput();

        return success(outputStream.toByteArray());
    }

    /**
     * 生成表
     * @param dto
     */
    @PostMapping("tables")
    public Result<Map<String, Integer>> tables(@RequestBody GenConfigDto dto) {
        final ListResult<LcdDesign> listResult = lcdDesignService.list(Wrappers.<LcdDesign>lambdaQuery()
                .select(LcdDesign::getSqlScript, LcdDesign::getId, LcdDesign::getTableName)
                .in(LcdDesign::getTableName, dto.getTables()));

        Map<String, Integer> map = new HashMap<>();
        if(!listResult.isEmpty()) {
            map.put("errorNum", 0);
            map.put("successNum", 0);

            Connection conn = properties.getLdc().getConn();
            try(Statement statement = conn.createStatement()) {
                listResult.forEach(item -> {
                    try {
                        final int status = statement.executeUpdate(item.getSqlScript());
                        if(status == 1) {
                            map.put("successNum", map.get("successNum") + 1);
                        } else {
                            map.put("errorNum", map.get("errorNum") + 1);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return success(map);
    }

    /**
     * 预览生成的源代码
     * @param tableName
     * @return
     */
    @GetMapping("review")
    public Result<ReviewDto> review(String tableName) {
        if(StrUtil.isBlank(tableName)) {
            return fail("请指定要预览的表");
        }

        DesignConfigBuilder configBuilder = new DesignConfigBuilder(properties.getLpc(), properties.getLdc()
                , properties.getLsc(), new TemplateConfig().setEntity("/templates/lcd.entity.java")
                , properties.getLgc(), Arrays.asList(tableName));

        final DesignReviewVelocityTemplateEngine engine = new DesignReviewVelocityTemplateEngine(properties);
        engine.init(pretreatmentConfigBuilder(configBuilder)).mkdirs().batchOutput();
        return success(engine.getReviewDto());
    }

    protected DesignConfigBuilder pretreatmentConfigBuilder(DesignConfigBuilder config) {

        /*
         * 表信息列表
         */
        List<TableInfo> tableList = config.getTableInfoList();
        for (TableInfo tableInfo : tableList) {
            /* ---------- 添加导入包 ---------- */
            if (config.getGlobalConfig().isActiveRecord()) {
                // 开启 ActiveRecord 模式
                tableInfo.setImportPackages(Model.class.getCanonicalName());
            }
            if (tableInfo.isConvert()) {
                // 表注解
                tableInfo.setImportPackages(TableName.class.getCanonicalName());
            }
            if (config.getStrategyConfig().getLogicDeleteFieldName() != null && tableInfo.isLogicDelete(config.getStrategyConfig().getLogicDeleteFieldName())) {
                // 逻辑删除注解
                tableInfo.setImportPackages(TableLogic.class.getCanonicalName());
            }
            if (StringUtils.isNotBlank(config.getStrategyConfig().getVersionFieldName())) {
                // 乐观锁注解
                tableInfo.setImportPackages(Version.class.getCanonicalName());
            }
            boolean importSerializable = true;
            if (StringUtils.isNotBlank(config.getStrategyConfig().getSuperEntityClass())) {
                // 父实体
                tableInfo.setImportPackages(config.getStrategyConfig().getSuperEntityClass());
                importSerializable = false;
            }
            if (config.getGlobalConfig().isActiveRecord()) {
                importSerializable = true;
            }
            if (importSerializable) {
                tableInfo.setImportPackages(Serializable.class.getCanonicalName());
            }
            // Boolean类型is前缀处理
            if (config.getStrategyConfig().isEntityBooleanColumnRemoveIsPrefix()
                    && CollectionUtils.isNotEmpty(tableInfo.getFields())) {
                List<TableField> tableFields = tableInfo.getFields().stream().filter(field -> "boolean".equalsIgnoreCase(field.getPropertyType()))
                        .filter(field -> field.getPropertyName().startsWith("is")).collect(Collectors.toList());
                tableFields.forEach(field -> {
                    //主键为is的情况基本上是不存在的.
                    if (field.isKeyFlag()) {
                        tableInfo.setImportPackages(TableId.class.getCanonicalName());
                    } else {
                        tableInfo.setImportPackages(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
                    }
                    field.setConvert(true);
                    field.setPropertyName(StringUtils.removePrefixAfterPrefixToLower(field.getPropertyName(), 2));
                });
            }
        }

        return config.setTableInfoList(tableList);
    }
}
