package com.iteaj.iboot.plugin.code.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.iteaj.framework.spi.admin.*;
import com.iteaj.iboot.plugin.code.LowCodeProperties;
import com.iteaj.iboot.plugin.code.VelocityReviewTemplateEngine;
import com.iteaj.iboot.plugin.code.dto.*;
import com.iteaj.iboot.plugin.code.vue.VelocityVueTemplateEngine;
import com.iteaj.iboot.plugin.code.vue.VelocityVueTemplateZipEngine;
import com.iteaj.iboot.plugin.code.utils.LowCodeUtil;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.result.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 代码生成器功能
 * @author iteaj
 */
@RestController
@RequestMapping("/lcd/gen")
public class GeneratorController extends BaseController implements InitializingBean {

    private String database;
    private final List<Module> modules;
    private final Environment environment;
    private final LowCodeProperties properties;
    private final ResourceManager resourceManager;
    private final FrameworkProperties frameworkProperties;
    private Pattern pattern = Pattern.compile("jdbc:.*((//)|@)(?<host>.+):(?<port>\\d+)/(?<db>.*)\\?.*");

    public GeneratorController(List<Module> modules, Environment environment
            , LowCodeProperties properties, ResourceManager resourceManager
            , FrameworkProperties frameworkProperties) {
        this.modules = modules;
        this.environment = environment;
        this.properties = properties;
        this.resourceManager = resourceManager;
        this.frameworkProperties = frameworkProperties;
    }

    /**
     * 获取数据库表列表
     * @return
     */
    @GetMapping("list")
    public Result<List<TableInfo>> list(final String name, final String prefix) {
        initTableQuerySql(name, prefix, null);
        List<TableInfo> tableInfoList = new ConfigBuilder(properties.getLpc(), properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), properties.getLgc()).getTableInfoList();
        if(CollectionUtil.isNotEmpty(tableInfoList)) {
            tableInfoList.forEach(item -> {
                item.setFields(null); // 不写字段信息到前端
                item.setCommonFields(null);
                item.setImportPackages(null);
            });
        }
        return success(tableInfoList);
    }

    /**
     * 初始化信息
     * @return
     */
    @GetMapping("init")
    public Result<GenInit> init() {
        GenInit genInit = new GenInit();
        genInit.setMsn(properties.getLpc().getModuleName());
        genInit.setParentPath(properties.getLpc().getParent());
        genInit.setProjectPath(properties.getProjectPath());
        genInit.setWebProjectPath(properties.getWebProjectPath());
        return success(genInit);
    }

    @GetMapping("prefix")
    public Result<List<Map<String, String>>> prefix() {
        return success(properties.getLsc().getTablePrefix().stream().map(item -> {
            Map<String, String> options = new HashMap<>();
            options.put("label", item);
            options.put("value", item);
            return options;
        }).collect(Collectors.toList()));
    }

    /**
     * 获取生成路径
     * @return
     */
    @GetMapping("getOutDir")
    public Result<GenDirDto> getOutDir() {
        return success(new GenDirDto(properties.getLgc().getOutputDir()
                , properties.getProjectPath())
                .setModuleName(properties.getLpc().getModuleName())
                .setParentPackage(properties.getLpc().getParent()));
    }

    /**
     * 获取当前系统模块路径
     * @return
     */
    @GetMapping("msn")
    public Result<List<ProjectPathDto>> getModules() {
        String projectPath = properties.getProjectPath();
        File file = new File(projectPath);
        List<ProjectPathDto> modules = new ArrayList<>();
        if(environment.acceptsProfiles(Profiles.of("dev"))) {
            if(file.exists() && file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(pathname -> {
                    if(pathname.isDirectory()) {
                        if(pathname.getName().equals("bootstrap")) {
                            ProjectPathDto bootstrap = new ProjectPathDto(pathname.getName(), pathname.getPath(), false);
                            File file1 = new File(pathname, "/src/main/java/com/iteaj/iboot/module");
                            if(file1.isDirectory()) {
                                File[] files = file1.listFiles();
                                for (File module : files) {
                                    bootstrap.addChildren(module.getName(), pathname.getPath(), module.getName(), false);
                                }
                            }

                            modules.add(bootstrap);
                        } else if(pathname.getName().equals("iboot-plugin")) {
                            ProjectPathDto plugins = new ProjectPathDto(pathname.getName(), pathname.getPath(), true);
                            Arrays.stream(pathname.listFiles()).forEach(fileItem -> {
                                if(fileItem.isDirectory()) {
                                    File file1 = new File(fileItem, "/src/main/java/com/iteaj/iboot/plugin");
                                    if(file1.isDirectory()) {
                                        String module = file1.list()[0];
                                        plugins.addChildren(fileItem.getName(), fileItem.getPath(), module, true);
                                    }
                                }
                            });

                            modules.add(plugins);
                        }
                    }
                });
            }
        } else {
            ProjectPathDto pathDto = new ProjectPathDto("bootstrap", "-", false);
            pathDto.addChildren("preview", "非开发环境仅做预览使用", false);
            modules.add(pathDto);
        }

        return success(modules);
    }

    /**
     * 查询父菜单
     * @return
     */
    @GetMapping("parent")
    public Result<Collection<MenuResource>> parentMenus() {
        return success(this.resourceManager.listResourcesByType(MenuType.M));
    }

    /**
     * 生成后端代码
     * @param dto
     */
    @PostMapping("java")
    public Result<Boolean> generator(@RequestBody GenConfigDto dto) {
        if(CollectionUtils.isEmpty(dto.getTables())) {
            return fail("请选择要生成的表");
        }

        initTableQuerySql(null, null, dto.getTables());
        GlobalConfig globalConfig = new GlobalConfig();

        BeanUtils.copyProperties(properties.getLgc(), globalConfig);
        // 设置输出目录
        globalConfig.setOutputDir(dto.getOutDir() + String.format("%ssrc%smain%sjava", File.separator, File.separator, File.separator));

        PackageConfig packageConfig = new PackageConfig();
        BeanUtils.copyProperties(properties.getLpc(), packageConfig);
        packageConfig.setModuleName(dto.getPackageName()).setParent(dto.getParent());

        // 文件是否覆盖
        globalConfig.setFileOverride(dto.isFileOverride());
        ConfigBuilder configBuilder = new ConfigBuilder(packageConfig, properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), globalConfig);

        configBuilder.setInjectionConfig(new ExtraInjectionConfig().setConfig(configBuilder));

        // 生成文件
        new AutoGenerator().setConfig(configBuilder).execute();

        // 生成菜单
        if(dto.getMenuId() != null) {
            genMenu(dto, configBuilder);
        }

        // 生成前端代码
        if(StringUtils.hasText(dto.getWebOutDir())) {
            String outDir = dto.getWebOutDir() + File.separator + "src" + File.separator + "views";
            configBuilder.getGlobalConfig().setOutputDir(outDir);
            new AutoGenerator()
                    .setTemplateEngine(new VelocityVueTemplateEngine(properties))
                    .setConfig(configBuilder).execute();
        }

        return success(true);
    }

    /**
     *  生成菜单
     * @param dto
     * @param configBuilder
     */
    private void genMenu(GenConfigDto dto, ConfigBuilder configBuilder) {
        if(dto.getMenuId() != null && CollectionUtil.isNotEmpty(configBuilder.getTableInfoList())) {
            StringBuilder url = new StringBuilder();
            // 获取父菜单的msn作为子菜单的msn
            dto.setMsn(resourceManager.getMsnByMenuId(dto.getMenuId()));

            Connection conn = properties.getLdc().getConn();
            try(Statement statement = conn.createStatement()) {
                configBuilder.getTableInfoList().forEach(tableInfo -> {
                    url.append("/").append(dto.getPackageName())
                            .append("/").append(tableInfo.getEntityPath());

                    // 菜单url已经存在
                    UrlResource byUrl = resourceManager.getByUrl(url.toString());
                    if(byUrl == null) {
                        // 写菜单到数据库
                        writeMenuToDb(tableInfo, dto, properties, statement);
                    }

                    url.setLength(0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeMenuToDb(TableInfo tableInfo, GenConfigDto dto, LowCodeProperties properties, Statement statement) {
        try {
            final String comment = LowCodeUtil.getModuleName(tableInfo.getComment());
            String url = "/"+dto.getPackageName()+"/" + tableInfo.getEntityPath();
            String perm = dto.getPackageName()+":"+tableInfo.getEntityPath();
            String insertSql= String.format("insert into sys_menu (pid, name, url, perms, type, msn, sort) value (%s, '%s', '%s', '%s', 'V', '%s', 158);"
                    , dto.getMenuId(), comment, url, perm+":"+"view", dto.getMsn());
            statement.executeUpdate(insertSql);

            // 查找刚刚插入的菜单id
            final ResultSet resultSet = statement.executeQuery("select last_insert_id()");
            resultSet.next();
            final long pid = resultSet.getLong(1);

            StringBuilder batchSql = new StringBuilder("insert into sys_menu (pid, name, url, perms, type, msn, sort) values ");
            // 查询列表
            batchSql.append(String.format("(%s, '%s', '%s', '%s', 'A', '%s', 5),", pid, "查询", url+"/view", perm+":view", dto.getMsn()));
            // 新增
            batchSql.append(String.format("(%s, '%s', '%s', '%s', 'A', '%s', 10),", pid, "新增", url+"/add", perm+":add", dto.getMsn()));
            // 修改
            batchSql.append(String.format("(%s, '%s', '%s', '%s', 'A', '%s', 15),", pid, "修改", url+"/edit", perm+":edit", dto.getMsn()));
            // 删除
            batchSql.append(String.format("(%s, '%s', '%s', '%s', 'A', '%s', 20);", pid, "删除", url+"/del", perm+":del", dto.getMsn()));
            statement.executeUpdate(batchSql.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成前端代码
     * @param dto
     */
    @PostMapping("vue")
    public void download(@RequestBody GenConfigDto dto, HttpServletResponse response) throws IOException {
        initTableQuerySql(null, null, dto.getTables());

        PackageConfig packageConfig = new PackageConfig();
        BeanUtils.copyProperties(properties.getLpc(), packageConfig);
        packageConfig.setModuleName(dto.getPackageName());

        ConfigBuilder configBuilder = new ConfigBuilder(packageConfig, properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), properties.getLgc());
        response.setContentType("application/octet-stream");

        // 生成文件
        ServletOutputStream outputStream = response.getOutputStream();
        new AutoGenerator().setTemplateEngine(new VelocityVueTemplateZipEngine(outputStream
                , properties)).setConfig(configBuilder).execute();
    }

    /**
     * 预览生成的源代码
     * @param dto
     * @return
     */
    @GetMapping("review")
    public Result<ReviewDto> review(GenConfigDto dto) {
        String tableName = dto.getTables().get(0);
        if(StrUtil.isBlank(tableName)) {
            return fail("请指定要预览的表");
        }

        String msn = dto.getMsn();
        if(!StringUtils.hasText(msn)) {
            msn = "-";
        }

        PackageConfig packageConfig = new PackageConfig();
        BeanUtils.copyProperties(properties.getLpc(), packageConfig);
        packageConfig.setModuleName(dto.getPackageName()).setParent(dto.getParent());

        initTableQuerySql(null, null, Arrays.asList(tableName));
        ConfigBuilder configBuilder = new ConfigBuilder(packageConfig, properties.getLdc()
                , properties.getLsc(), new TemplateConfig(), properties.getLgc());

        // 生成文件
        VelocityReviewTemplateEngine engine = new VelocityReviewTemplateEngine(properties);
        new AutoGenerator().setConfig(configBuilder).setTemplateEngine(engine).execute();

        final ReviewDto reviewDto = engine.getReviewDto();
        reviewDto.setMenuSql(this.getMenuSql(configBuilder, msn, dto.getPackageName()));
        return success(reviewDto);
    }

    private String getMenuSql(ConfigBuilder configBuilder, String msn, String packageName) {
        final TableInfo tableInfo = configBuilder.getTableInfoList().get(0);
        final String comment = LowCodeUtil.getModuleName(tableInfo.getComment());
        String url = "/"+packageName+"/" + tableInfo.getEntityPath();
        String perm = packageName+":"+tableInfo.getEntityPath();

        StringBuilder batchSql = new StringBuilder();
        batchSql.append(String.format("insert into sys_menu (pid, name, url, perms, type, remark, msn, sort) \n\tvalue (%s, '%s管理', '%s', '%s', 'V', '浏览%s', '%s', 158);\n", 0, comment, url, perm+":"+"view", comment, msn));
        batchSql.append("set @pid = (select last_insert_id());\n");
        batchSql.append("insert into sys_menu (pid, name, url, perms, type, remark, msn, sort) values \n");
        // 查询列表
        batchSql.append(String.format("\t(%s, '%s', '%s', '%s', 'A', '查询', '%s', 5),\n", "@pid", "查询", url+"/view", perm+":view", msn));
        // 新增
        batchSql.append(String.format("\t(%s, '%s', '%s', '%s', 'A', '新增', '%s', 10),\n", "@pid", "新增", url+"/add", perm+":add", msn));
        // 修改
        batchSql.append(String.format("\t(%s, '%s', '%s', '%s', 'A', '修改', '%s', 15),\n", "@pid", "修改", url+"/edit", perm+":edit", msn));
        // 删除
        batchSql.append(String.format("\t(%s, '%s', '%s', '%s', 'A', '删除', '%s', 20);", "@pid", "删除", url+"/del", perm+":del", msn));
        return batchSql.toString();
    }

    private void initTableQuerySql(String name, String prefix, List<String> tables) {
        properties.getLdc().setDbQuery(new MySqlQuery() {
            @Override
            public String tablesSql() {
                StringBuilder sql = new StringBuilder("select * from information_schema.tables where TABLE_SCHEMA='").append(database).append("'");

                if(StrUtil.isNotBlank(name) && StrUtil.isNotBlank(prefix)) {
                    sql.append("and TABLE_NAME LIKE '%").append(name).append("%' and TABLE_NAME LIKE ").append(prefix).append("%");
                } else if(StrUtil.isNotBlank(name)) {
                    sql.append("and TABLE_NAME LIKE '%").append(name).append("%'");
                } else if(StrUtil.isNotBlank(prefix)) {
                    sql.append("and TABLE_NAME LIKE '").append(prefix).append("%'");
                } else if(!CollectionUtils.isEmpty(tables)) {
                    String collect = tables.stream().map(item -> "'" + item + "'").collect(Collectors.joining(","));
                    sql.append("and TABLE_NAME in (").append(collect).append(")");
                }

                return sql.append(" order by CREATE_TIME desc").toString();
            }

            @Override
            public String[] fieldCustom() {
                return new String[] {"key", "null", "default"};
            }

            @Override
            public String tableName() {
                return "TABLE_NAME";
            }

            @Override
            public String tableComment() {
                return "TABLE_COMMENT";
            }

            @Override
            public boolean isKeyIdentity(ResultSet results) throws SQLException {
                return false;
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 数据源配置
        LowCodeProperties.LcdDataSourceConfig ldc = properties.getLdc();
        Matcher matcher = pattern.matcher(ldc.getUrl());
        if(matcher.find()) {
            this.database = matcher.group("db");
        }

        if(StrUtil.isBlank(this.database)) {
            throw new BeanInitializationException("没有指定代码生成器所要生成的数据库");
        }
    }
}
