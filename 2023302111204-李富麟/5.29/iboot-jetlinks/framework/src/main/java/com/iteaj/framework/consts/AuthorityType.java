package com.iteaj.framework.consts;

public enum AuthorityType {
    dir("目录"), menu("菜单"), function("权限");

    private String desc;

    AuthorityType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
