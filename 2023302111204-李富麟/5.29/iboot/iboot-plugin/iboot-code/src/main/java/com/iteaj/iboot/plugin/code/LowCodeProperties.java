package com.iteaj.iboot.plugin.code;

import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Set;

/**
 * 低代码配置
 */
@Data
@ConfigurationProperties("low.code")
public class LowCodeProperties implements InitializingBean {

    /**
     * 全局配置
     */
    private LcdGlobalConfig lgc;
    /**
     * 生成包信息配置
     */
    private LcdPackageConfig lpc;
    /**
     * 生成策略配置
     */
    private LcdStrategyConfig lsc;
    /**
     * 数据源配置
     */
    private LcdDataSourceConfig ldc;
    /**
     * 搜索配置
     */
    private LcdFormConfig form;

    /**
     * 本地项目根路径
     */
    private String projectPath;

    /**
     * 前端项目根路径
     */
    private String webProjectPath;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(this.getLpc() == null) {
            this.lpc = new LcdPackageConfig();
        }

        if(StringUtils.hasText(this.getProjectPath())) {
            // 获取父路径
            File file = new File(this.getProjectPath());
            if(file.exists() && file.isDirectory()) {
                this.projectPath = file.getParentFile().getParentFile().getPath();
            }
        }
    }

    public static class LcdGlobalConfig extends GlobalConfig { }
    public static class LcdPackageConfig extends PackageConfig { }
    public static class LcdStrategyConfig extends StrategyConfig { }

    /**
     * 前端表单配置
     */
    @Data
    public static class LcdFormConfig {

        /**
         * 表字段不包含以下内容加入编辑组件
         */
        private Set<String> edit;

        /**
         * 表字段包含以下内容加入搜索组件
         */
        private Set<String> search;
    }

    @Data
    public static class LcdDataSourceConfig extends DataSourceConfig { }
}
