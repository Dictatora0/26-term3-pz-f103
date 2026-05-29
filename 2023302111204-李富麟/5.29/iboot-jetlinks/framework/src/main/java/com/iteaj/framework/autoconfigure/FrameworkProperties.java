package com.iteaj.framework.autoconfigure;

import com.iteaj.framework.captcha.CaptchaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "framework")
public class FrameworkProperties {

    /**
     * 当前系统访问地址,不包含uri (http://www.iteaj.com)
     */
    private String domain;

    /**
     * 是否启用集群模式
     */
    private boolean cluster;

    /**
     * 版本
     */
    private String version;

    /**
     * web环境配置
     */
    private Web web = new Web();

    /**
     * 启用的模块
     * 配置格式如下：<hr>
     *     framework.profiles.core=true
     *     framework.profiles.framework=true
     */
    private Map<String, Boolean> profiles;

    /**
     * 校验码配置
     */
    private CaptchaConfig captcha = new CaptchaConfig();

    @Data
    public static class Web {

        /**
         * 启用日志记录
         */
        private boolean logger;

        /**
         * 文件上传配置
         */
        private Upload upload = new Upload();

        /**
         * session配置
         */
        private Session session = new Session();
    }

    /**
     * 文件上传配置
     */
    @Data
    public static class Upload {

        /**
         * 文件访问pattern如 /file/**
         */
        private String pattern = String.format("%sfile%s**", File.separator, File.separator);

        /**
         * 文件存放路径必须以file: 或 classpath: 开头
         */
        private String location = String.format("file:D:%supload%s", File.separator, File.separator);

        public String getLocationPath() {
            return location.contains("file:") ? location.split("file:")[1] : location.split("classpath:")[1];
        }
    }

    @Data
    public static class Session {

        /**
         * token过期时间(秒)
         */
        private long timeout = 30 * 60;

        /**
         * token是否写到header(为true优先读取header)
         */
        private boolean writeHeader = false;

        /**
         * 自动选择token(浏览器使用cookie, app使用header)
         */
        private boolean automaticToken = false;

        /**
         * 缓存名称
         */
        private String name = "IBoot:Session";

        /**
         * token名称
         */
        private String tokenName = "access_token";
    }

    @Data
    public static class CaptchaConfig {

        /**
         * 有效期(秒)
         * 默认1分钟
         */
        private long expire = 1 * 60;

        private CaptchaType type = CaptchaType.Math;
    }
}
