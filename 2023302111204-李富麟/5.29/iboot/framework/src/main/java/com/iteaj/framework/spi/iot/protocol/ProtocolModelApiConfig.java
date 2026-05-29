package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.spi.iot.consts.ApiConfigDirection;
import com.iteaj.framework.spi.iot.consts.ApiFieldType;

import java.util.ArrayList;
import java.util.List;

/**
 * 协议模型接口配置
 */
public class ProtocolModelApiConfig {

    /**
     * 排序
     */
    private int sort;

    /**
     * 长度
     */
    private int length;

    /**
     * 说明
     */
    private String remark;

    /**
     * 点位地址
     */
    private String position;

    /**
     * 字段类型
     */
    private ApiFieldType fieldType;

    /**
     * 属性方向
     */
    private ApiConfigDirection direction;

    /**
     * 属性代码
     */
    private String protocolModelAttrField;

    /**
     * 下拉款配置项
     */
    private List<ProtocolModelApiConfigOption> options;

    public ProtocolModelApiConfig(String remark, String protocolModelAttrField) {
        this(remark, protocolModelAttrField, 0);
    }

    public ProtocolModelApiConfig(String remark, String protocolModelAttrField, int sort) {
        this.sort = sort;
        this.remark = remark;
        this.fieldType = ApiFieldType.field;
        this.protocolModelAttrField = protocolModelAttrField;
    }

    public ProtocolModelApiConfig(String protocolModelAttrField, String remark, ApiConfigDirection direction) {
        this(protocolModelAttrField, remark, direction, 0);
    }

    public ProtocolModelApiConfig(String protocolModelAttrField, String remark, ApiConfigDirection direction, int sort) {
        this.sort = sort;
        this.remark = remark;
        this.direction = direction;
        this.fieldType = ApiFieldType.field;
        this.protocolModelAttrField = protocolModelAttrField;
    }

    public ProtocolModelApiConfig(int length, String remark, String protocolModelAttrField) {
        this.length = length;
        this.remark = remark;
        this.fieldType = ApiFieldType.field;
        this.protocolModelAttrField = protocolModelAttrField;
    }

    public ProtocolModelApiConfig(int sort, int length, String remark, String position, String protocolModelAttrField) {
        this.sort = sort;
        this.length = length;
        this.remark = remark;
        this.position = position;
        this.fieldType = ApiFieldType.field;
        this.protocolModelAttrField = protocolModelAttrField;
    }

    public static ProtocolModelApiConfig upBuild(String attrField) {
        return upBuild(attrField, null, 0);
    }

    public static ProtocolModelApiConfig upBuild(String attrField, String remark) {
        return upBuild(attrField, remark, 0);
    }

    public static ProtocolModelApiConfig upBuild(String attrField, String remark, int sort) {
        return new ProtocolModelApiConfig(attrField, remark, ApiConfigDirection.UP, sort);
    }

    public static ProtocolModelApiConfig upStatusBuild(String attrField) {
        return upBuild(attrField, null, 0).setFieldType(ApiFieldType.status);
    }

    public static ProtocolModelApiConfig upStatusBuild(String attrField, String remark) {
        return upBuild(attrField, remark, 0).setFieldType(ApiFieldType.status);
    }

    public static ProtocolModelApiConfig upStatusBuild(String attrField, String remark, int sort) {
        return upBuild(attrField, remark, sort).setFieldType(ApiFieldType.status);
    }

    public static ProtocolModelApiConfig downBuild(String attrField) {
        return downBuild(attrField, null, 0);
    }

    public static ProtocolModelApiConfig downBuild(String attrField, String remark) {
        return downBuild(attrField, remark, 0);
    }

    public static ProtocolModelApiConfig downBuild(String attrField, String remark, int sort) {
        return new ProtocolModelApiConfig(attrField, remark, ApiConfigDirection.DOWN, sort);
    }

    public ProtocolModelApiConfig addOption(String label, Object value) {
        return this.addOption(label, value, "");
    }

    public ProtocolModelApiConfig addOption(String label, Object value, String extra) {
        if(this.getOptions() == null) {
            this.options = new ArrayList<>();
        }

        this.options.add(new ProtocolModelApiConfigOption(label, value));
        return this;
    }

    public int getSort() {
        return sort;
    }

    public ProtocolModelApiConfig setSort(int sort) {
        this.sort = sort; return this;
    }

    public ApiFieldType getFieldType() {
        return fieldType;
    }

    public ProtocolModelApiConfig setFieldType(ApiFieldType fieldType) {
        this.fieldType = fieldType; return this;
    }

    public int getLength() {
        return length;
    }

    public ProtocolModelApiConfig setLength(int length) {
        this.length = length; return this;
    }

    public String getRemark() {
        return remark;
    }

    public ProtocolModelApiConfig setRemark(String remark) {
        this.remark = remark; return this;
    }

    public String getProtocolModelAttrField() {
        return protocolModelAttrField;
    }

    public ProtocolModelApiConfig setProtocolModelAttrField(String protocolModelAttrField) {
        this.protocolModelAttrField = protocolModelAttrField; return this;
    }

    public String getPosition() {
        return position;
    }

    public ProtocolModelApiConfig setPosition(String position) {
        this.position = position; return this;
    }

    public ApiConfigDirection getDirection() {
        return direction;
    }

    public ProtocolModelApiConfig setDirection(ApiConfigDirection direction) {
        this.direction = direction; return this;
    }

    public List<ProtocolModelApiConfigOption> getOptions() {
        return options;
    }

    public ProtocolModelApiConfig setOptions(List<ProtocolModelApiConfigOption> options) {
        this.options = options; return this;
    }
}
