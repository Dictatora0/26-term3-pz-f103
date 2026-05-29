package com.iteaj.iboot.module.iot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.framework.IVOption;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class PanelsDeviceDto {

    /**
     * 控制设备状态控制项
     */
    private List<IVOption> options;

    /**
     * 控制值
     */
    private String ctrlValue;

    /**
     * 各属性值
     */
    private List<AttrValue> values;

    /**
     * 设备
     */
    private DeviceDto device;

    /**
     * 产品信息
     */
    private ProductDto product;

    public PanelsDeviceDto(List<IVOption> options, List<AttrValue> values, DeviceDto device) {
        this.options = options;
        this.values = values;
        this.device = device;
    }

    @Getter
    @Setter
    public static class AttrValue {
        /**
         * 属性字段
         */
        private String field;
        /**
         * 属性名称
         */
        private String name;
        /**
         * 属性单位
         */
        private String unit = "";

        /**
         * 属性最新值
         */
        private String value = "-";

        /**
         * 值标签
         */
        private String label = null;

        @JsonIgnore
        private Boolean ctrlStatus;

        public AttrValue(String field, String name, String unit, Boolean ctrlStatus) {
            this.field = field;
            this.name = name;
            this.unit = unit;
            this.ctrlStatus = ctrlStatus;
        }
    }
}
