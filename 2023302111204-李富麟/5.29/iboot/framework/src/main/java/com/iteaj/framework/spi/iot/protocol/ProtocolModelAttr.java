package com.iteaj.framework.spi.iot.protocol;

import com.iteaj.framework.spi.iot.consts.AttrType;
import com.iteaj.framework.spi.iot.consts.DataType;

/**
 * 协议模型属性
 */
public class ProtocolModelAttr {

    /**
     * 属性代码
     */
    private String field;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性类型
     */
    private DataType dataType;

    /**
     * 读写方式
     */
    private AttrType attrType;

    /**
     * 属性说明
     */
    private String remark;

    public ProtocolModelAttr(String field, String name, DataType type, AttrType attrType) {
        this(field, name, type, attrType, null);
    }

    public ProtocolModelAttr(String field, String name, DataType type, AttrType attrType, String remark) {
        this.name = name;
        this.dataType = type;
        this.field = field;
        this.remark = remark;
        this.attrType = attrType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public AttrType getAttrType() {
        return attrType;
    }

    public void setAttrType(AttrType attrType) {
        this.attrType = attrType;
    }
}
