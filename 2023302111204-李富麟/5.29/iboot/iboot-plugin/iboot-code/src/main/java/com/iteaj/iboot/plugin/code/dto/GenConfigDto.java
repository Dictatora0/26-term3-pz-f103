package com.iteaj.iboot.plugin.code.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenConfigDto {

    /**
     * 菜单msn
     */
    private String msn;

    /**
     * 父包名
     */
    private String parent;

    /**
     * 模块包
     */
    private String packageName;

    /**
     * 父菜单的id
     */
    private Long menuId;

    /**
     * 输出目录
     */
    private String outDir;

    /**
     * 要生成的表名列表
     */
    private List<String> tables;

    /**
     * 是否导入菜单
     */
    private boolean importMenu;

    /**
     * 文件存在是否覆盖
     */
    private boolean fileOverride;

    /**
     * web端代码输出目录
     */
    private String webOutDir;
}
