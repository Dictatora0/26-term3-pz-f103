package com.iteaj.framework;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamMeta {

    /**
     * 字段名
     */
    private String field;

    /**
     * 字段说明
     */
    private String label;

    /**
     * 额外说明
     */
    private String extra;

    /**
     * 值类型(string, number, hidden)
     */
    private String type;

    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 默认值
     */
    private Object defaultValue;

    private String placeholder;

    /**
     * 配置值
     */
    private IVOption[] options;

    public ParamMeta(String field, String label) {
        this(field, label, false);
    }

    public ParamMeta(String field, String label, Boolean required) {
        this(field, label, null, required, null);
    }

    public ParamMeta(String field, String label, String extra) {
        this(field, label, extra, false, null);
    }

    public ParamMeta(String field, String label, String extra, Boolean required, Object defaultValue) {
        this(field, label, extra, required, defaultValue, "string");
    }

    public ParamMeta(String field, String label, String extra, Boolean required, Object defaultValue, String type) {
        this.type = type;
        this.field = field;
        this.label = label;
        this.extra = extra;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public static ParamMeta build(String field) {
        return new ParamMeta(field, field);
    }

    public static ParamMeta build(String field, String label) {
        return new ParamMeta(field, label);
    }

    public static ParamMeta buildDefault(String field, String label, String defaultValue) {
        return buildDefault(field, label, null, defaultValue);
    }

    public static ParamMeta buildDefault(String field, String label, String extra, String defaultValue) {
        return new ParamMeta(field, label, extra, false, defaultValue);
    }

    public static ParamMeta build(String field, String label, String extra) {
        return new ParamMeta(field, label, extra);
    }

    public static ParamMeta buildRequired(String field, String label) {
        return new ParamMeta(field, label, true);
    }

    public static ParamMeta buildRequired(String field, String label, String extra, String defaultValue) {
        return new ParamMeta(field, label, extra, true, defaultValue);
    }

    public static ParamMeta buildNumber(String field, String label, Object defaultValue) {
        return buildNumber(field, label, defaultValue, null);
    }

    public static ParamMeta buildNumber(String field, String label, Object defaultValue, String extra) {
        return new ParamMeta(field, label, extra, false, defaultValue, "number");
    }

    public static ParamMeta buildRequiredNumber(String field, String label, Object defaultValue, String extra) {
        return new ParamMeta(field, label, extra, true, defaultValue, "number");
    }

    public static ParamMeta buildSelect(String field, String label, String extra, IVOption... options) {
        return new ParamMeta(field, label, extra, true, null, "select").setOptions(options);
    }

    public static ParamMeta buildRequiredSelect(String field, String label, String defaultValue, String extra, IVOption... options) {
        return new ParamMeta(field, label, extra, true, defaultValue, "select").setOptions(options);
    }

    public ParamMeta setPlaceholder(String placeholder) {
        this.placeholder = placeholder; return this;
    }
}
