package com.iteaj.iboot.module.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 行政区域
 */
@Data
@TableName("sys_region")
@Accessors(chain = true)
public class Region extends BaseEntity {

    /**
     * 行政区码
     */
    private String code;

    /**
     * 行政区域名称
     */
    private String name;

    /**
     * 首字母
     */
    private String firstLetter;

    /**
     * 父级id
     */
    private Long pid;

    /**
     * 等级(第一级为0)
     */
    private Integer level;
}
