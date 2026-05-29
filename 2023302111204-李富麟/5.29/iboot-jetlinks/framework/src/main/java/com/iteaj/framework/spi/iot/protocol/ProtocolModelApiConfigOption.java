package com.iteaj.framework.spi.iot.protocol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProtocolModelApiConfigOption {

    /**
     * 标签
     */
    private String label;

    /**
     * 值
     */
    private Object value;

//    /**
//     * 是否禁用
//     */
//    private boolean disabled;
//
//    /**
//     * 是否默认选中
//     */
//    private boolean checked;

    /**
     * 额外扩展信息
     */
    private Object extra;

    public ProtocolModelApiConfigOption(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public ProtocolModelApiConfigOption(String label, Object value, Object extra) {
        this.label = label;
        this.value = value;
        this.extra = extra;
    }
}
