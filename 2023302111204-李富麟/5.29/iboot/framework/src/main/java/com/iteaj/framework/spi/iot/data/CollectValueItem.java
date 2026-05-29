package com.iteaj.framework.spi.iot.data;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CollectValueItem {

    /**
     * 总条数
     */
    private long total;

    /**
     * 字段
     */
    private String field;

    /**
     * 采集值
     */
    private List<?> values;

    protected CollectValueItem(String field, List<?> values) {
        this.field = field;
        this.values = values;
    }

    public static CollectValueItem build(String field, List<?> values) {
        return new CollectValueItem(field, values);
    }

    public static CollectValueItem build(String field, List<?> values, long total) {
        return new CollectValueItem(field, values).setTotal(total);
    }
}
