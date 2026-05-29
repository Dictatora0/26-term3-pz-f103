package com.iteaj.iboot.plugin.code.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenDirDto {

    /**
     * 自定义生成目录
     */
    private String cusGenDir;

    /**
     * 项目根目录
     */
    private String projectRoot;

    /**
     * 父包
     */
    private String parentPackage;

    /**
     * 模块名
     */
    private String moduleName;

    public GenDirDto(String cusGenDir, String projectRoot) {
        this.cusGenDir = cusGenDir;
        this.projectRoot = projectRoot;
    }
}
