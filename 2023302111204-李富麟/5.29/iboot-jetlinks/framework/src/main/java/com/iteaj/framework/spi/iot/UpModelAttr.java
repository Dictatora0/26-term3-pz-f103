package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.DataType;
import lombok.Getter;
import lombok.Setter;

/**
 * 上行模型属性
 */
@Getter
@Setter
public class UpModelAttr {

    private Long id;

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
     * 精度
     */
    private Integer accuracy;

    /**
     * 数据增益
     */
    private Integer gain;

    /**
     * 数据解析器
     */
    private String resolver;

    /**
     * 自定义解析脚本
     */
    private String script;

    /**
     * json解析路径
     */
    private String path;

    public UpModelAttr(Long id, String field, String name, DataType dataType
            , Integer accuracy, Integer gain, String resolver, String script, String path) {
        this.id = id;
        this.field = field;
        this.name = name;
        this.gain = gain;
        this.path = path;
        this.script = script;
        this.resolver = resolver;
        this.accuracy = accuracy;
        this.dataType = dataType;
    }
}
