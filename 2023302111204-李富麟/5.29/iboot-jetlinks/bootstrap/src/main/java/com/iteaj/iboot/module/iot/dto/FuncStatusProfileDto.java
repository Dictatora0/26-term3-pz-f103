package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能状态统计dto
 */
@Getter
@Setter
public class FuncStatusProfileDto {

    /**
     * 总数量
     */
    private int totalNum;

    /**
     * 启用数量
     */
    private int enabledNum;
}
