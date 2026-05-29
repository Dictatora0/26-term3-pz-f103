package com.iteaj.framework.spi.iot.consts;

public enum DataType {

    t_any("any", "any(任何)"), // 前端上行配置项可以编辑
    t_json("json", "json类型"), // 前端上行配置项可以编辑
    t_byte("byte", "byte(字节)"),
    t_short("short", "short(短整型)"),
    t_int("int", "int(整型)"),
    t_long("long", "long(长整型)"),
    t_float("float", "float(单精度)"),
    t_double("double", "double(双精度)"),
    t_date("date", "date(日期)"),
    t_time("time", "time(时间)"),
    t_datetime("datetime", "datetime(完整时间)"),
    t_timestamp("timestamp", "timestamp(时间戳)"),
    t_string("string", "string(字符串)"),
    t_boolean("boolean", "boolean(布尔)"),
    ;

    private String value;
    private String desc;

    DataType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static DataType build(String dataType) {
        if(dataType == null) {
            return null;
        }

        switch (dataType) {
            case "byte": return t_byte;
            case "short": return t_short;
            case "int": return t_int;
            case "long": return t_long;
            case "float": return t_float;
            case "double": return t_double;
            case "date": return t_date;
            case "time": return t_time;
            case "timestamp": return t_timestamp;
            case "string": return t_string;
            case "boolean": return t_boolean;
            default: throw new IllegalArgumentException("未知的数据类型["+dataType+"]");
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
