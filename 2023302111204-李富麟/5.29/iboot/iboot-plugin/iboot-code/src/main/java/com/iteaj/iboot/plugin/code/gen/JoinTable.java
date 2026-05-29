package com.iteaj.iboot.plugin.code.gen;

import lombok.Data;

@Data
public class JoinTable {

    /**
     * 表明
     */
    private String name;

    /**
     * 表别名
     */
    private String alias;

    /**
     * 引用字段
     */
    private String foreignId;

    private String field;

    public JoinTable(String name, String alias, String foreignId, String field) {
        this.name = name;
        this.alias = alias;
        this.field = field;
        this.foreignId = foreignId;
    }
}
