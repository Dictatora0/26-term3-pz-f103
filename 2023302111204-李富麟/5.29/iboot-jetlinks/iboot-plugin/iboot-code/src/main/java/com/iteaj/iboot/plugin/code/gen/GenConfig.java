package com.iteaj.iboot.plugin.code.gen;

/**
 * create time: 2020/7/2
 *
 * @author iteaj
 * @since 1.0
 */
public class GenConfig {

    /**
     * 要挂载的父菜单
     */
    private Integer menuId;

    /**
     * 模块别名, 默认为项目名称
     *  模块别名将作为包路径、url路径、权限路径的一部分
     */
    private String alias;

    /**
     * 父项目
     */
    private String parent;

    /**
     * 项目名称
     */
    private String project;

    /**
     * 是否直接生成文件到项目
     */
    private boolean write;

    /**
     * 是否可以覆盖文件
     */
    private boolean overwrite;

    /**
     * 要生成的文件
     * ENTITY、MAPPER、XML、SERVICE、SERVICE_IMPL、CONTROLLER、OTHER
     */
    private String[] writeType;

    private String outRootDir; // 生成的文件输出的根路径

    public String getAlias() {
        return alias;
    }

    public GenConfig setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getProject() {
        return project;
    }

    public GenConfig setProject(String project) {
        this.project = project;
        return this;
    }

    public boolean isWrite() {
        return write;
    }

    public GenConfig setWrite(boolean write) {
        this.write = write;
        return this;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public GenConfig setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public String[] getWriteType() {
        return writeType;
    }

    public GenConfig setWriteType(String[] writeType) {
        this.writeType = writeType;
        return this;
    }

    public String getOutRootDir() {
        return outRootDir;
    }

    public GenConfig setOutRootDir(String outRootDir) {
        this.outRootDir = outRootDir;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public GenConfig setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public Integer getMenuId() {
        return menuId;
    }

    public GenConfig setMenuId(Integer menuId) {
        this.menuId = menuId;
        return this;
    }
}
