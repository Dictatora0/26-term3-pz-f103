package com.iteaj.iboot.plugin.code.gen;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.spring.SpringUtils;
import com.iteaj.iboot.plugin.code.LowCodeConst;
import com.iteaj.iboot.plugin.code.design.LcdInjectionConfig;
import com.iteaj.iboot.plugin.code.entity.LcdDesign;
import com.iteaj.iboot.plugin.code.service.ILcdDesignService;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class DesignConfigBuilder {

    private DataSourceConfig dataSourceConfig;

    /**
     * 模板路径配置信息
     */
    private final TemplateConfig template;

    /**
     * 数据库表信息
     */
    private List<TableInfo> tableInfoList;
    /**
     * 包配置详情
     */
    private Map<String, String> packageInfo;
    /**
     * 路径配置信息
     */
    private Map<String, String> pathInfo;
    /**
     * 策略配置
     */
    private StrategyConfig strategyConfig;
    /**
     * 全局配置信息
     */
    private GlobalConfig globalConfig;
    /**
     * 注入配置信息
     */
    private LcdInjectionConfig injectionConfig;
    /**
     * 过滤正则
     */
    private static final Pattern REGX = Pattern.compile("[~!/@#$%^&*()+\\\\\\[\\]|{};:'\",<.>?]+");

    /**
     * 在构造器中处理配置
     *
     * @param packageConfig    包配置
     * @param strategyConfig   表配置
     * @param template         模板配置
     * @param globalConfig     全局配置
     */
    public DesignConfigBuilder(PackageConfig packageConfig, DataSourceConfig dataSourceConfig, StrategyConfig strategyConfig
            , TemplateConfig template, GlobalConfig globalConfig, List<String> tableNames) {

        // 全局配置
        this.globalConfig = Optional.ofNullable(globalConfig).orElseGet(GlobalConfig::new);
        // 模板配置
        this.template = Optional.ofNullable(template).orElseGet(TemplateConfig::new);

        this.template.setXml("/templates/lcd.mapper.xml");
        this.template.setEntity("/templates/lcd.entity.java");
        this.template.setMapper("/templates/lcd.mapper.java");
        this.template.setService("/templates/lcd.service.java");
        this.template.setController("/templates/lcd.controller.java");
        this.template.setServiceImpl("/templates/lcd.serviceImpl.java");

        // 包配置
        if (null == packageConfig) {
            handlerPackage(this.template, this.globalConfig.getOutputDir(), new PackageConfig());
        } else {
            handlerPackage(this.template, this.globalConfig.getOutputDir(), packageConfig);
        }
        this.dataSourceConfig = dataSourceConfig;
        // 策略配置
        this.strategyConfig = Optional.ofNullable(strategyConfig).orElseGet(StrategyConfig::new);

        this.tableInfoList = getTablesInfo(strategyConfig, tableNames);
    }


    // ************************ 曝露方法 BEGIN*****************************

    /**
     * 所有包配置信息
     *
     * @return 包配置
     */
    public Map<String, String> getPackageInfo() {
        return packageInfo;
    }


    /**
     * 所有路径配置
     *
     * @return 路径配置
     */
    public Map<String, String> getPathInfo() {
        return pathInfo;
    }

    /**
     * 表信息
     *
     * @return 所有表信息
     */
    public List<TableInfo> getTableInfoList() {
        return tableInfoList;
    }

    public DesignConfigBuilder setTableInfoList(List<TableInfo> tableInfoList) {
        this.tableInfoList = tableInfoList;
        return this;
    }

    /**
     * 模板路径配置信息
     *
     * @return 所以模板路径配置信息
     */
    public TemplateConfig getTemplate() {
        return this.template;
    }

    // ****************************** 曝露方法 END**********************************

    /**
     * 处理包配置
     *
     * @param template  TemplateConfig
     * @param outputDir
     * @param config    PackageConfig
     */
    protected void handlerPackage(TemplateConfig template, String outputDir, PackageConfig config) {
        // 包信息
        packageInfo = CollectionUtils.newHashMapWithExpectedSize(7);
        packageInfo.put(ConstVal.MODULE_NAME, config.getModuleName());
        packageInfo.put(LowCodeConst.DTO, joinPackage(config.getParent(), "dto"));
        packageInfo.put(ConstVal.ENTITY, joinPackage(config.getParent(), config.getEntity()));
        packageInfo.put(ConstVal.MAPPER, joinPackage(config.getParent(), config.getMapper()));
        packageInfo.put(ConstVal.XML, joinPackage(config.getParent(), config.getXml()));
        packageInfo.put(ConstVal.SERVICE, joinPackage(config.getParent(), config.getService()));
        packageInfo.put(ConstVal.SERVICE_IMPL, joinPackage(config.getParent(), config.getServiceImpl()));
        packageInfo.put(ConstVal.CONTROLLER, joinPackage(config.getParent(), config.getController()));

        // 自定义路径
        Map<String, String> configPathInfo = config.getPathInfo();
        if (null != configPathInfo) {
            pathInfo = configPathInfo;
        } else {
            // 生成路径信息
            pathInfo = CollectionUtils.newHashMapWithExpectedSize(6);
            setPathInfo(pathInfo, template.getEntity(getGlobalConfig().isKotlin()), outputDir, ConstVal.ENTITY_PATH, ConstVal.ENTITY);
            setPathInfo(pathInfo, template.getMapper(), outputDir, ConstVal.MAPPER_PATH, ConstVal.MAPPER);
            setPathInfo(pathInfo, template.getXml(), outputDir, ConstVal.XML_PATH, ConstVal.XML);
            setPathInfo(pathInfo, "lcd.dto.java", outputDir, LowCodeConst.DTO_PATH, LowCodeConst.DTO);
            setPathInfo(pathInfo, template.getService(), outputDir, ConstVal.SERVICE_PATH, ConstVal.SERVICE);
            setPathInfo(pathInfo, template.getServiceImpl(), outputDir, ConstVal.SERVICE_IMPL_PATH, ConstVal.SERVICE_IMPL);
            setPathInfo(pathInfo, template.getController(), outputDir, ConstVal.CONTROLLER_PATH, ConstVal.CONTROLLER);
        }
    }

    protected void setPathInfo(Map<String, String> pathInfo, String template, String outputDir, String path, String module) {
        if (StringUtils.isNotBlank(template)) {
            pathInfo.put(path, joinPath(outputDir, packageInfo.get(module)));
        }
    }

    /**
     * 处理表对应的类名称
     *
     * @param tableList 表名称
     * @param config    策略配置项
     * @return 补充完整信息后的表
     */
    protected List<TableInfo> processTable(List<TableInfo> tableList, StrategyConfig config) {
        for (TableInfo tableInfo : tableList) {
            String entityName;
            INameConvert nameConvert = strategyConfig.getNameConvert();
            if (null != nameConvert) {
                // 自定义处理实体名称
                entityName = nameConvert.entityNameConvert(tableInfo);
            } else {
                entityName = NamingStrategy.capitalFirst(processName(tableInfo.getName(), config.getNaming(), config.getTablePrefix()));
            }
            if (StringUtils.isNotBlank(globalConfig.getEntityName())) {
                tableInfo.setConvert(true);
                tableInfo.setEntityName(String.format(globalConfig.getEntityName(), entityName));
            } else {
                tableInfo.setEntityName(strategyConfig, entityName);
            }
            if (StringUtils.isNotBlank(globalConfig.getMapperName())) {
                tableInfo.setMapperName(String.format(globalConfig.getMapperName(), entityName));
            } else {
                tableInfo.setMapperName(entityName + ConstVal.MAPPER);
            }
            if (StringUtils.isNotBlank(globalConfig.getXmlName())) {
                tableInfo.setXmlName(String.format(globalConfig.getXmlName(), entityName));
            } else {
                tableInfo.setXmlName(entityName + ConstVal.MAPPER);
            }
            if (StringUtils.isNotBlank(globalConfig.getServiceName())) {
                tableInfo.setServiceName(String.format(globalConfig.getServiceName(), entityName));
            } else {
                tableInfo.setServiceName("I" + entityName + ConstVal.SERVICE);
            }
            if (StringUtils.isNotBlank(globalConfig.getServiceImplName())) {
                tableInfo.setServiceImplName(String.format(globalConfig.getServiceImplName(), entityName));
            } else {
                tableInfo.setServiceImplName(entityName + ConstVal.SERVICE_IMPL);
            }
            if (StringUtils.isNotBlank(globalConfig.getControllerName())) {
                tableInfo.setControllerName(String.format(globalConfig.getControllerName(), entityName));
            } else {
                tableInfo.setControllerName(entityName + ConstVal.CONTROLLER);
            }
            // 检测导入包
            checkImportPackages(tableInfo);
        }
        return tableList;
    }

    /**
     * 检测导入包
     *
     * @param tableInfo ignore
     */
    protected void checkImportPackages(TableInfo tableInfo) {
        if (StringUtils.isNotBlank(strategyConfig.getSuperEntityClass())) {
            // 自定义父类
            tableInfo.getImportPackages().add(strategyConfig.getSuperEntityClass());
        } else if (globalConfig.isActiveRecord()) {
            // 无父类开启 AR 模式
            tableInfo.getImportPackages().add(com.baomidou.mybatisplus.extension.activerecord.Model.class.getCanonicalName());
        }
        if (null != globalConfig.getIdType() && tableInfo.isHavePrimaryKey()) {
            // 指定需要 IdType 场景
            tableInfo.getImportPackages().add(IdType.class.getCanonicalName());
            tableInfo.getImportPackages().add(TableId.class.getCanonicalName());
        }
        if (StringUtils.isNotBlank(strategyConfig.getVersionFieldName())
                && CollectionUtils.isNotEmpty(tableInfo.getFields())) {
            tableInfo.getFields().forEach(f -> {
                if (strategyConfig.getVersionFieldName().equals(f.getName())) {
                    tableInfo.getImportPackages().add(com.baomidou.mybatisplus.annotation.Version.class.getCanonicalName());
                }
            });
        }
    }


    /**
     * 获取所有的数据库表信息
     */
    protected List<TableInfo> getTablesInfo(StrategyConfig config, List<String> tableNames) {
        List<TableInfo> tableInfos = new ArrayList<>();
        ILcdDesignService designService = SpringUtils.getBean(ILcdDesignService.class);
        ListResult<LcdDesign> listResult = designService.list(Wrappers
                .<LcdDesign>lambdaQuery().in(LcdDesign::getTableName, tableNames));

        listResult.forEach(item -> {
            DesignTableInfo tableInfo = new DesignTableInfo();
            tableInfo.setComment(item.getComment()).setConvert(true).setName(item.getTableName());

            tableInfos.add(convertTableFields(tableInfo, item));
        });

        return processTable(tableInfos, strategyConfig);
    }

    /**
     * 表名匹配
     *
     * @param setTableName 设置表名
     * @param dbTableName  数据库表单
     * @return ignore
     */
    protected boolean tableNameMatches(String setTableName, String dbTableName) {
        return setTableName.equalsIgnoreCase(dbTableName)
                || StringUtils.matches(setTableName, dbTableName);
    }

    /**
     * 将字段信息与表信息关联
     *
     * @param tableInfo 表信息
     * @param entity    配置实体
     * @return ignore
     */
    protected TableInfo convertTableFields(DesignTableInfo tableInfo, LcdDesign entity) {
        boolean haveId = false;
        List<TableField> fieldList = new ArrayList<>();
        List<LcdField> dtoFields = new ArrayList<>();
        List<LcdField> foreign = new ArrayList<>(); // 外键字段
        List<FormField> formFields = new ArrayList<>();
        List<FormField> searchFields = new ArrayList<>();
        List<JoinTable> joinTables = new ArrayList<>();
        Map<String, Character> tableAlias = new HashMap<>();
        List<FormTableField> formTableFields = new ArrayList<>();

        Map<String, LcdField> idMaps = new HashMap<>();
        List<TableField> commonFieldList = new ArrayList<>();

        char startAlias = 'a'; // 别名从a开始
        AtomicInteger aliasSetup = new AtomicInteger(1);
        tableAlias.put(tableInfo.getName(), startAlias);

        // 处理主键信息
        JsonNode container = entity.getContainer().get("meta");
        if(container != null && !container.isNull()) {
            JsonNode tabModel = container.get("tabModel");
            if(tabModel instanceof ObjectNode) {
                JsonNode keyField = tabModel.get("keyField");
                if(keyField!=null && !keyField.isNull()) {
                    LcdField lcdField = new LcdField();
                    String field = keyField.asText();
                    lcdField.setName(field);
                    lcdField.setKeyFlag(true);
                    lcdField.setType(tabModel.get("keyType").asText());
                    lcdField.setComment(tabModel.get("comment").asText());
                    tableInfo.setKeyField(field);
                    handleFieldConvert(lcdField);
                    if (strategyConfig.includeSuperEntityColumns(lcdField.getName())) {
                        // 跳过公共字段
                        commonFieldList.add(lcdField);
                    } else {
                        fieldList.add(lcdField);
                    }
                }
            }
        }

        // 处理编辑字段信息
        ArrayNode metas = entity.getEdit().withArray("metas");
        metas.forEach(item -> {
            LcdField field = new LcdField();
            ObjectNode meta = (ObjectNode) item;
            ObjectNode tabModel = (ObjectNode) meta.get("tabModel");

            idMaps.put(meta.get("id").asText(), field);
            String columnName = tabModel.get("field").asText();
            String title = tabModel.get("title").asText();
            String type = tabModel.get("type").asText();

            // 处理其它信息
            field.setName(columnName);
            field.setTitle(title);
            String newColumnName = columnName;
            field.setFormType(type);
            field.setTable(tableInfo.getName());
            field.setType(tabModel.get("fieldType").asText());

            IKeyWordsHandler keyWordsHandler = dataSourceConfig.getKeyWordsHandler();
            if (keyWordsHandler != null && keyWordsHandler.isKeyWords(columnName)) {
                System.err.printf("当前表[%s]存在字段[%s]为数据库关键字或保留字!%n", tableInfo.getName(), columnName);
                field.setKeyWords(true);
                newColumnName = keyWordsHandler.formatColumn(columnName);
            }

            field.setColumnName(newColumnName);

            // 处理字段的转换问题
            handleFieldConvert(field);

            // excel注解处理
            handleExcel(tableInfo, field, tabModel, title);

            // valid注解处理
            handleValidate(tableInfo, field, tabModel, title, type);

            // 数据库字段
            if(tabModel.get("isDb").asBoolean()) {
                fieldList.add(field);
                field.setComment(tabModel.get("comment").asText());

                // 收集外键列表
                if("foreign".equals(type)) { // 如果是外键类型
                    foreign.add(field);
                    // 用abc作为表别名
                    String quoteTable = tabModel.get("quoteTable").asText();
                    if(!tableAlias.containsKey(quoteTable)) {
                        // 别名新增1
                        tableAlias.put(quoteTable, (char) (startAlias + aliasSetup.getAndIncrement()));
                    }

                    // 新增关联的表
                    joinTables.add(new JoinTable(quoteTable, tableAlias.get(quoteTable).toString()
                            , tabModel.get("foreignId").asText(), tabModel.get("field").asText()));
                }

                if (strategyConfig.includeSuperEntityColumns(field.getName())) {
                    // 跳过公共字段

                    commonFieldList.add(field);
                    return;
                }

            // 引用类型的字段都是dto字段
            } else if("quote".equals(type)) {
                String quoteTable = tabModel.get("quoteTable").asText();
                field.setAlias(tableAlias.get(quoteTable).toString());
                dtoFields.add(field); // 引用字典列表
                field.setTable(tabModel.get("quoteTable").asText()); // 设置所属表
                // 导入需要的包
                if(field.getColumnType().getPkg() != null) {
                    tableInfo.getDtoImportPackages().add(field.getColumnType().getPkg());
                }

                JsonNode target = tabModel.get("target");
                if(target instanceof ObjectNode) {
                    field.setComment(target.get("comment").asText(""));
                }

                if(StringUtils.isNotBlank(field.getExcel())) {
                    tableInfo.getDtoImportPackages().add("cn.afterturn.easypoi.excel.annotation.*");
                }
            }
        });
        // 处理ivzone编辑组件
        final ArrayNode edit = entity.getIvzone().withArray("edit");
        if(edit != null) {
            edit.forEach(item -> {
                final String id = item.get("id").asText();
                final LcdField tableField = idMaps.get(id);
                formFields.add(new FormField().setComponent(String.format(item.get("template").asText()
                        , tableField.getPropertyName(), tableField.getTitle())));
            });
        }

        // 处理ivzone搜索组件
        Map<String, String> searchIdTitleMaps = new HashMap<>();
        entity.getSearch().withArray("metas").forEach(meta -> searchIdTitleMaps
                .put(meta.get("id").asText(), meta.get("tabModel").get("title").asText()));
        final JsonNode search = entity.getIvzone().withArray("search");
        if(search != null) {
            search.forEach(item -> {
                final String id = item.get("id").asText();
                final LcdField tableField = idMaps.get(id);
                FormField formField = new FormField().setComponent(String.format(item.get("template").asText()
                        , tableField.getPropertyName(), searchIdTitleMaps.get(id))).setTable(tableField.getTable())
                        .setAlias(tableAlias.get(tableField.getTable()).toString()).setColumnName(tableField.getColumnName())
                        .setPropertyName(tableField.getPropertyName()).setType(tableField.getColumnType());

                searchFields.add(formField);
            });
        }

        // 处理ivzone表格组件columns
        final JsonNode table = entity.getIvzone().withArray("table");
        if(table != null) {
            table.forEach(item -> {
                final String id = item.get("id").asText();
                final TableField tableField = idMaps.get(id);
                FormTableField formTableField = new FormTableField();
                final String template = item.get("template").asText();
                if(id.equalsIgnoreCase("action")) {
                    formTableField.setComponent(String.format(template, "action"));
                } else {
                    formTableField.setComponent(String.format(template, tableField.getPropertyName()));
                }

                formTableFields.add(formTableField);
            });
        }

        tableInfo.setFields(fieldList);
        tableInfo.setDtoFields(dtoFields);
        tableInfo.setFormFields(formFields);
        tableInfo.setJoinTables(joinTables);
        tableInfo.setSearchFields(searchFields);
        tableInfo.setCommonFields(commonFieldList);
        tableInfo.setFormTableFields(formTableFields);

        return tableInfo;
    }

    private void handleFieldConvert(LcdField field) {
        INameConvert nameConvert = strategyConfig.getNameConvert();
        if (null != nameConvert) {
            field.setPropertyName(nameConvert.propertyNameConvert(field));
        } else {
            field.setPropertyName(strategyConfig, processName(field.getName(), strategyConfig.getColumnNaming()));
        }

        field.setColumnType(dataSourceConfig.getTypeConvert().processTypeConvert(globalConfig, field));

        // 填充逻辑判断
        List<TableFill> tableFillList = getStrategyConfig().getTableFillList();
        if (null != tableFillList) {
            // 忽略大写字段问题
            tableFillList.stream().filter(tf -> tf.getFieldName().equalsIgnoreCase(field.getName()))
                    .findFirst().ifPresent(tf -> field.setFill(tf.getFieldFill().name()));
        }
    }

    private void handleExcel(DesignTableInfo tableInfo, LcdField field, ObjectNode tabModel, String title) {
        final JsonNode anImport = tabModel.get("isImport");
        final JsonNode anExport = tabModel.get("isExport");

        if(anImport != null && anExport != null) {
            Boolean isImport = anImport.asBoolean(false);
            Boolean isExport = anExport.asBoolean(false);

            if(isImport || isExport) {
                StringBuilder excel = new StringBuilder("@Excel(name=").append('"').append(title).append('"');
                if(isImport) {
                    excel.append(", isImportField=\"true\"");
                }
                excel.append(")");
                field.setExcel(excel.toString());
                tableInfo.getImportPackages().add("cn.afterturn.easypoi.excel.annotation.*");
            }
        }
    }

    private void handleValidate(DesignTableInfo tableInfo, LcdField field, ObjectNode tabModel, String title, String type) {
        JsonNode rules = tabModel.get("rules");
        if(rules instanceof ArrayNode) {
            JsonNode jsonNode = rules.get(0);
            StringBuilder validate = new StringBuilder();
            if(jsonNode instanceof ObjectNode) {
                boolean required = jsonNode.get("required").asBoolean();
                if(required) {
                    if(type.equals("number") || type.equals("slider") || type.equals("rate")) {
                        validate.append("@NotNull(message=").append('"').append(title).append("必填").append('"').append(")");
                    } else {
                        validate.append("@NotBlank(message=").append('"').append(title).append("必填").append('"').append(")");
                    }
                }

                field.setValidate(validate.toString());
                tableInfo.getImportPackages().add("javax.validation.constraints.*");
            }
        }
    }

    /**
     * 连接路径字符串
     *
     * @param parentDir   路径常量字符串
     * @param packageName 包名
     * @return 连接后的路径
     */
    protected String joinPath(String parentDir, String packageName) {
        if (StringUtils.isBlank(parentDir)) {
            parentDir = System.getProperty(ConstVal.JAVA_TMPDIR);
        }
        if (!StringUtils.endsWith(parentDir, File.separator)) {
            parentDir += File.separator;
        }
        packageName = packageName.replaceAll("\\.", StringPool.BACK_SLASH + File.separator);
        return parentDir + packageName;
    }


    /**
     * 连接父子包名
     *
     * @param parent     父包名
     * @param subPackage 子包名
     * @return 连接后的包名
     */
    protected String joinPackage(String parent, String subPackage) {
        return StringUtils.isBlank(parent) ? subPackage : (parent + StringPool.DOT + subPackage);
    }


    /**
     * 处理字段名称
     *
     * @return 根据策略返回处理后的名称
     */
    protected String processName(String name, NamingStrategy strategy) {
        return processName(name, strategy, strategyConfig.getFieldPrefix());
    }


    /**
     * 处理表/字段名称
     *
     * @param name     ignore
     * @param strategy ignore
     * @param prefix   ignore
     * @return 根据策略返回处理后的名称
     */
    protected String processName(String name, NamingStrategy strategy, Set<String> prefix) {
        String propertyName;
        if (prefix.size() > 0) {
            if (strategy == NamingStrategy.underline_to_camel) {
                // 删除前缀、下划线转驼峰
                propertyName = NamingStrategy.removePrefixAndCamel(name, prefix);
            } else {
                // 删除前缀
                propertyName = NamingStrategy.removePrefix(name, prefix);
            }
        } else if (strategy == NamingStrategy.underline_to_camel) {
            // 下划线转驼峰
            propertyName = NamingStrategy.underlineToCamel(name);
        } else {
            // 不处理
            propertyName = name;
        }
        return propertyName;
    }


    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }


    public DesignConfigBuilder setStrategyConfig(StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
        return this;
    }


    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }


    public DesignConfigBuilder setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        return this;
    }


    public LcdInjectionConfig getInjectionConfig() {
        return injectionConfig;
    }


    public DesignConfigBuilder setInjectionConfig(LcdInjectionConfig injectionConfig) {
        this.injectionConfig = injectionConfig;
        return this;
    }

    /**
     * 格式化数据库注释内容
     *
     * @param comment 注释
     * @return 注释
     * @since 3.4.0
     */
    public String formatComment(String comment) {
        return StringUtils.isBlank(comment) ? StringPool.EMPTY : comment.replaceAll("\r\n", "\t");
    }
}
