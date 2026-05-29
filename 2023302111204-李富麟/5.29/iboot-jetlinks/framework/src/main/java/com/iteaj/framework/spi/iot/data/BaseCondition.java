package com.iteaj.framework.spi.iot.data;

import cn.hutool.core.collection.CollectionUtil;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.consts.TimeCountCondition;
import com.iteaj.framework.spi.iot.consts.TimeCountType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class BaseCondition {

    /**
     * 时间类型
     * @see #timeRange
     */
    private TimeCountType timeType;

    /**
     * 时间范围(格式 yyyy-MM-dd HH:mm)
     * 1. 支持具体的时间 如：2024-01-10 15:00 - 2024-01-16 15:00
     * 2. 支持now关键字 如：now-6 - now
     * @see #timeType
     */
    private List<String> timeRange;

    /**
     * 业务类型值 格式为 type:param:option  支持以下几种类型
     * 1. 设备 D:uid:field[,field]
     * 2. 产品 P:productCode:field[,field]
     * 3. 字段 F:field[,field]
     */
    private List<String> value;

    protected void validateCondition() {
        if(this.getTimeType() == null) {
            throw new ServiceException("时间类型未指定[timeType]");
        }

        if(CollectionUtil.isEmpty(this.getTimeRange())) {
            throw new ServiceException("未指定时间范围[timeRange]");
        }

        if(this.getTimeRange().size() != 2) {
            throw new ServiceException("时间范围格式错误");
        }
    }

    public List<BaseCondition.ValueMeta> resolverValue(List<String> values) {
        List<BaseCondition.ValueMeta> metas = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(values)) {
            values.forEach(item -> {
                String key = item.replaceAll(" ", "");
                String[] split = key.split(":");
                if(split.length < 2) {
                    throw new ServiceException("不支持的格式["+item+"]");
                }

                String type = split[0].toUpperCase(Locale.ROOT);
                if(type.equals("D") || type.equals("P")) {
                    if(split.length == 2) {
                        metas.add(new BaseCondition.ValueMeta(type, split[1]));
                    } else {
                        metas.add(new BaseCondition.ValueMeta(type, split[1], Arrays.asList(split[2].split(","))));
                    }
                } else if(type.equals("F")) {
                    metas.add(new BaseCondition.ValueMeta(Arrays.asList(split[1].split(","))));
                } else {
                    throw new ServiceException("不支持的类型["+type+"]");
                }
            });
        }

        return metas;
    }


    @Data
    public class ValueMeta {
        /**
         * 类型
         * D 设备
         * P 产品
         * F 字段
         */
        private String type;

        /**
         * 设备uid, 产品编号
         */
        private String value;

        /**
         * 字段列表
         */
        private List<String> fields;

        public ValueMeta(List<String> fields) {
            this.type = "F";
            this.fields = fields;
        }

        public ValueMeta(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public ValueMeta(String type, String value, List<String> fields) {
            this.type = type;
            this.value = value;
            this.fields = fields;
        }
    }
}
