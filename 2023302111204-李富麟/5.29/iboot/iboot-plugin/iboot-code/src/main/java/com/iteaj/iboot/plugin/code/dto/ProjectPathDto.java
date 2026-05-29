package com.iteaj.iboot.plugin.code.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectPathDto {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目路径
     */
    private String path;

    /**
     * 模块包名
     */
    private String module;

    /**
     * 是否是插件
     */
    private boolean plugin;

    private List<ProjectPathDto> children = new ArrayList<>();

    public ProjectPathDto(String name, String path, boolean plugin) {
        this(name, path, name, plugin);
    }

    public ProjectPathDto(String name, String path, String module, boolean plugin) {
        this.name = name;
        this.path = path;
        this.module = module;
        this.plugin = plugin;
    }

    public void addChildren(String name, String path, boolean plugin) {
        this.children.add(new ProjectPathDto(name, path, plugin));
    }

    public void addChildren(String name, String path, String module, boolean plugin) {
        this.children.add(new ProjectPathDto(name, path, module, plugin));
    }
}
