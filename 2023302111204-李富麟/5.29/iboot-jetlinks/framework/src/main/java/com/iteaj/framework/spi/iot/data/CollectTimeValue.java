package com.iteaj.framework.spi.iot.data;

import lombok.Data;

@Data
public class CollectTimeValue {

    /**
     * 属性
     */
    private String field;

    /**
     * 值
     */
    private String value;

    /**
     * 时间
     */
    private String time;
}
