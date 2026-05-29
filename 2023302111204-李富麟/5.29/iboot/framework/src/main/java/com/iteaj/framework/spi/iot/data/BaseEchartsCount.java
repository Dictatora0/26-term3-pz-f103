package com.iteaj.framework.spi.iot.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseEchartsCount {

    private Object value;

    private String field;

    private String category;

    public BaseEchartsCount() { }

    public BaseEchartsCount(String category, Object value) {
        this.category = category;
        this.value = value;
    }

    public static BaseEchartsCount buildZero(String time) {
        return new BaseEchartsCount(time, 0);
    }
}
