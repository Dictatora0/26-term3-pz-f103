package com.iteaj.framework.spi.iot;

import com.iteaj.framework.spi.iot.consts.DataType;

/**
 * 数据类型解析器
 */
public interface DataValueResolver {

    String name();

    Object resolver(UpModelAttr attr, Object value);

    default boolean isInteger(DataType dataType) {
        return dataType == DataType.t_int || dataType == DataType.t_byte
                || dataType == DataType.t_long || dataType == DataType.t_short;
    }

    default boolean isNumber(DataType dataType) {
        return dataType == DataType.t_int || dataType == DataType.t_double || dataType == DataType.t_byte
                || dataType == DataType.t_long || dataType == DataType.t_short || dataType == DataType.t_float;
    }
}
