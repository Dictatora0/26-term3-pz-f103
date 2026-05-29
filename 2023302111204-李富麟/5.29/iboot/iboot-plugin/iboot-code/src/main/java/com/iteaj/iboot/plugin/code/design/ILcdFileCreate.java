package com.iteaj.iboot.plugin.code.design;

import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.iteaj.iboot.plugin.code.gen.DesignConfigBuilder;

import java.io.File;

public interface ILcdFileCreate {
    /**
     * 自定义判断是否创建文件
     *
     * @param configBuilder 配置构建器
     * @param fileType      文件类型
     * @param filePath      文件路径
     * @return ignore
     */
    boolean isCreate(DesignConfigBuilder configBuilder, FileType fileType, String filePath);

    /**
     * 检查文件目录，不存在自动递归创建
     *
     * @param filePath 文件路径
     */
    default void checkDir(String filePath) {
        File file = new File(filePath);
        boolean exist = file.exists();
        if (!exist) {
            file.getParentFile().mkdir();
        }
    }
}
