package com.iteaj.iboot.plugin.code.gen;

import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FormField {

    private IColumnType type;

    /**
     * xml 条件判断
     */
    private String condition;

    /**
     * 实体名
     */
    private String entity;

    /**
     * 表列名
     */
    private String columnName;

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 表别名
     */
    private String alias;

    /**
     * 应用的表
     */
    private String table;

    private String component;

    public String getCondition() {
        if(type != null) {
            if(this.type == DbColumnType.STRING) {
                return String.format("entity.%s!=null and entity.%s!=''", this.propertyName, this.propertyName);
            } else {
                return "entity." + this.propertyName+" != null";
            }
        }
        return "";
    }
}
