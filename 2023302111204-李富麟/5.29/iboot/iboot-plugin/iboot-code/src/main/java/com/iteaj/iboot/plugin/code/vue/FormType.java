package com.iteaj.iboot.plugin.code.vue;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;

/**
 * form 表单类型
 */
public enum FormType {
    Date("<UDatePickerItem field=\"%s\" label=\"%s\" %s/>", false),
    Time("<UTimePickerItem field=\"%s\" label=\"%s\" %s/>", false),
    Month("<UMonthPickerItem field=\"%s\" label=\"%s\" %s/>", false),
    DateTime("<UDatePickerItem field=\"%s\" label=\"%s\" show-time %s/>", false),
    Input("<UInputItem field=\"%s\" label=\"%s\" %s/>", false),
    Radio("<URadioItem field=\"%s\" label=\"%s\" %s/>", true),
    Rate("<URateItem field=\"%s\" label=\"%s\" %s/>", false),
    Cascade("<UCascaderItem field=\"%s\" label=\"%s\" %s/>", true),
    Checkbox("<UCheckboxItem field=\"%s\" label=\"%s\" %s/>", true),
    Slider("<USliderItem field=\"%s\" label=\"%s\" %s/>", false),
    Switch("<USwitchItem field=\"%s\" label=\"%s\" %s/>", false),
    InputNumber("<UInputNumberItem field=\"%s\" label=\"%s\" %s/>", false),
    Textarea("<UTextareaItem field=\"%s\" label=\"%s\" %s/>", false),
    Password("<UPasswordItem field=\"%s\" label=\"%s\" %s/>", false),
    Dict("<USelectItem field=\"%s\" label=\"%s\" %s/>", true),
    Select("<USelectItem field=\"%s\" label=\"%s\" %s/>", true),
    Mentions("<UMentionsItem field=\"%s\" label=\"%s\" %s/>", false),
    TreeSelect("<UTreeSelectItem field=\"%s\" label=\"%s\" %s/>", true),
    AutoComplete("<UAutoCompleteItem field=\"%s\" label=\"%s\" %s/>", true),
    ;

    private String template;
    private boolean options;

    FormType(String template, boolean options) {
        this.options = options;
        this.template = template;
    }

    public static FormType getType(DbColumnType columnType, String comment) {
        switch (columnType) {
            case BYTE:
            case LONG:
            case FLOAT:
            case SHORT:
            case DOUBLE:
            case INTEGER:
            case BASE_INT:
            case BASE_BYTE:
            case BASE_LONG:
            case BASE_FLOAT:
            case BASE_DOUBLE:
            case BIG_INTEGER:
            case BIG_DECIMAL:
            case BASE_SHORT:
                return InputNumber;
            case TIME:
            case LOCAL_TIME:
                return Time;
            case YEAR_MONTH:
                return Month;
            case DATE:
            case YEAR:
            case DATE_SQL:
            case LOCAL_DATE:
            case INSTANT:
                return Date;
            case TIMESTAMP:
            case LOCAL_DATE_TIME:
                return DateTime;
            case BOOLEAN:
            case BASE_BOOLEAN:
                return Radio;
            case CLOB:
                return Textarea;
            default:
                if(StrUtil.isNotBlank(comment)) {
                    if(comment.contains("字典")) {
                        return Dict;
                    }
                }
                return Input;
        }
    }

    public static FormType getType(String type) {
        switch (type) {
            case "text": return Input;
            case "radio": return Radio;
            case "rate": return Rate;
            case "checkbox": return Checkbox;
            case "slider": return Slider;
            case "select": return Select;
            case "switch": return Switch;
            case "password": return Password;
            case "textarea": return Textarea;
            case "cascade": return Cascade;
            case "stree": return TreeSelect;
            case "number": return InputNumber;
            case "mentions": return Mentions;
            case "autoComplete": return AutoComplete;
            default: return null;
        }
    }

    public String getTemplate(String... args) {
        return String.format(template, args);
    }

    public boolean isOptions() {
        return options;
    }
}
