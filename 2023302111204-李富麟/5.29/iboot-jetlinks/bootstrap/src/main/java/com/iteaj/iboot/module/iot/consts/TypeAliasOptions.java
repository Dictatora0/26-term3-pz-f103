package com.iteaj.iboot.module.iot.consts;

import lombok.Data;

/**
 * 设备类型别名下拉列表
 * @see DeviceTypeAlias
 */
@Data
public class TypeAliasOptions {

    /**
     * 名称
     */
    private String label;

    /**
     * 值
     */
    private String value;

    public TypeAliasOptions(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
