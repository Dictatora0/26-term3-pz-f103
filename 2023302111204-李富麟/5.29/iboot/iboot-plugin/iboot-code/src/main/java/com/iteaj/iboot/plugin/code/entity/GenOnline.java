package com.iteaj.iboot.plugin.code.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iteaj.framework.BaseEntity;
import com.iteaj.framework.mybatis.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author iteaj
 * @since 2020-09-13
 */
@Data
@Accessors(chain = true)
@TableName("t_gen_online")
public class GenOnline extends BaseEntity {


    /**
     * 功能说明
     */
    private String remark;

    private String html;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ArrayNode metas;

    /**
     * 功能名称
     */
    private String name;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ArrayNode permMetas;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ObjectNode viewConfig;


}
