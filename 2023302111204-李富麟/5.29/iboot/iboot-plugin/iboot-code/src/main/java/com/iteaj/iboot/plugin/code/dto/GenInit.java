package com.iteaj.iboot.plugin.code.dto;

import lombok.Data;

@Data
public class GenInit {

    /**
     * 当前模块名称
     */
    private String msn;

    /**
     * 父路径
     */
    private String parentPath;

    /**
     * 当前项目路径
     */
    private String projectPath;

    /**
     * 前端项目路径
     */
    private String webProjectPath;
}
