package com.iteaj.framework.spi.iot.data;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EchartsCountValue {

    /**
     * 设备uid
     */
    private String uid;

    /**
     * 统计字段
     */
    private String field;

    /**
     * 统计数据
     */
    private List<BaseEchartsCount> data;

    public EchartsCountValue(List<BaseEchartsCount> data) {
        this.data = data;
    }

    public EchartsCountValue(String uid, List<BaseEchartsCount> data) {
        this.uid = uid;
        this.data = data;
    }

    public EchartsCountValue(String uid, String field, List<BaseEchartsCount> data) {
        this.uid = uid;
        this.field = field;
        this.data = data;
    }
}
