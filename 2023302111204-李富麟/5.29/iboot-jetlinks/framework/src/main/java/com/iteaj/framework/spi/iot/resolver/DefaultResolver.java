package com.iteaj.framework.spi.iot.resolver;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.iteaj.framework.spi.iot.DataValueResolver;
import com.iteaj.framework.spi.iot.UpModelAttr;
import com.iteaj.framework.spi.iot.consts.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class DefaultResolver implements DataValueResolver {

    private static final String DEFAULT_NAME = "默认";
    private Logger logger = LoggerFactory.getLogger(DefaultResolver.class);

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public Object resolver(UpModelAttr attr, Object value) {
        DataType dataType = attr.getDataType();
        try {
            if(value instanceof Number) {
                if(value instanceof Long) {
                    if(dataType == DataType.t_datetime) {
                        return new Date((Long) value);
                    }
                }

                return doResolverNumberValue(attr, value);
            } else if(value instanceof String) {
                if(dataType == DataType.t_date) {
                    return DateUtil.parse(value.toString(), DatePattern.NORM_DATETIME_PATTERN);
                } else if(dataType == DataType.t_double || dataType == DataType.t_float) {
                    return doResolverNumberValue(attr, Double.valueOf(value.toString()));
                } else if(isInteger(dataType)) {
                    return doResolverNumberValue(attr, Long.valueOf(value.toString()));
                }
            }
        } catch (Exception e) {
            logger.error("解析数据格式异常({}) 解析器: {} - 属性: {} - 值: {}", dataType
                    , attr.getResolver(), attr.getField(), value, e);
        }
        return value;
    }

    private Object doResolverNumberValue(UpModelAttr attr, Object value) {
        if(attr.getGain() != null && attr.getGain() != 0) {
            value = Double.valueOf(value.toString()) / attr.getGain();
        }

        if(attr.getAccuracy() != null && attr.getAccuracy() > 0) {
            if(attr.getDataType() == DataType.t_double || attr.getDataType() == DataType.t_float) {
                BigDecimal bigDecimal = BigDecimal.valueOf(Double.valueOf(value.toString()));
                value = bigDecimal.setScale(attr.getAccuracy(), RoundingMode.DOWN).doubleValue();
            }
        }

        return value;
    }
}
