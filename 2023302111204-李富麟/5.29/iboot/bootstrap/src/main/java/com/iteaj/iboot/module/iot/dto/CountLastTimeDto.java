package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 最近一段时间数据
 */
@Getter
@Setter
public class CountLastTimeDto {

    /**
     * 时间
     */
    private String time;

    /**
     * 值
     */
    private Object value;

    public CountLastTimeDto() { }

    public CountLastTimeDto(String time, Object value) {
        this.time = time;
        this.value = value;
    }

    public static CountLastTimeDto buildZero(String time) {
        return new CountLastTimeDto(time, 0);
    }
}
