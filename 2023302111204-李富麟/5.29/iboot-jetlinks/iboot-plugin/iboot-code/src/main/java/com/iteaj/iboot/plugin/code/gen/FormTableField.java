package com.iteaj.iboot.plugin.code.gen;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FormTableField {

    /**
     * 类型
     */
    private String type;

    /**
     * 组件
     */
    private String component;
}
