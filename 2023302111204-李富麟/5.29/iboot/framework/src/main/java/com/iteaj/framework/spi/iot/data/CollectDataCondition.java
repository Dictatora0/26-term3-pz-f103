package com.iteaj.framework.spi.iot.data;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.consts.CollectSearchType;
import com.iteaj.framework.spi.iot.consts.TimeCountType;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CollectDataCondition extends BaseCondition{

    /**
     * 每页条数
     * @see CollectSearchType#page
     */
    private int size;

    /**
     * 当前页
     * @see CollectSearchType#page
     */
    private int current;

    /**
     * 搜索类型
     */
    private CollectSearchType searchType;

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

    public String[] resolverTime() {
        String[] result = new String[]{this.getTimeRange().get(0), this.getTimeRange().get(1)};
        TimeCountType timeType = this.getTimeType();
        DateTime startTime, endTime;
        int startOffset = 0, endOffset = 0;
        if(result[0].trim().equalsIgnoreCase("now")) {
            startTime = new DateTime();
        } else if(result[0].contains("now")) {
            startTime = new DateTime();
            String replaceAll = result[0].replaceAll(" ", "");
            if(replaceAll.contains("+")) {
                startOffset = 0 + Integer.valueOf(replaceAll.substring(4));
            } else if(replaceAll.contains("-")) {
                startOffset = 0 - Integer.valueOf(replaceAll.substring(4));
            } else {
                throw new ServiceException("不支持的时间操作符");
            }
        } else {
            startTime = DateUtil.parse(result[0], "yyyy-MM-dd HH");
        }

        if(result[1].trim().equalsIgnoreCase("now")) {
            endTime = new DateTime(); endOffset = endOffset + 1;
        } else if(result[1].contains("now")) {
            endTime = new DateTime();
            String replaceAll = result[1].replaceAll(" ", "");
            if(replaceAll.contains("+")) {
                endOffset = 1 + Integer.valueOf(replaceAll.substring(4));
            } else if(replaceAll.contains("-")) {
                endOffset = 1 - Integer.valueOf(replaceAll.substring(4));
            } else {
                throw new ServiceException("不支持的时间操作符");
            }
        } else {
            endOffset = endOffset + 1;
            endTime = DateUtil.parse(result[1], "yyyy-MM-dd HH");
        }

        if(timeType == TimeCountType.year) {
            DateTime startDateTime = startTime.offset(DateField.YEAR, startOffset).setField(DateField.MONTH, 0)
                    .setField(DateField.DAY_OF_MONTH, 1).setField(DateField.HOUR_OF_DAY, 0)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.YEAR, endOffset).setField(DateField.MONTH, 0)
                    .setField(DateField.DAY_OF_MONTH, 1).setField(DateField.HOUR_OF_DAY, 0)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
        } else if(timeType == TimeCountType.quarter) {

        } else if(timeType == TimeCountType.month) {
            DateTime startDateTime = startTime.offset(DateField.MONTH, startOffset)
                    .setField(DateField.DAY_OF_MONTH, 1).setField(DateField.HOUR_OF_DAY, 0)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.MONTH, endOffset)
                    .setField(DateField.DAY_OF_MONTH, 1).setField(DateField.HOUR_OF_DAY, 0)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
        } else if(timeType == TimeCountType.week) {
            DateTime startDateTime = startTime.offset(DateField.WEEK_OF_MONTH, startOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.WEEK_OF_MONTH, endOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
        } else if(timeType == TimeCountType.day) {
            DateTime startDateTime = startTime.offset(DateField.DAY_OF_MONTH, startOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.DAY_OF_MONTH, endOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
        } else if(timeType == TimeCountType.hour) {
            DateTime startDateTime = startTime.offset(DateField.HOUR_OF_DAY, startOffset)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDatetime = endTime.offset(DateField.HOUR_OF_DAY, endOffset)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDatetime.getTime() - 1);
        } else {
            throw new ServiceException("不支持的时间类型["+this.getTimeType()+"]");
        }

        return result;
    }

    public List<DataSqlCondition> buildSqlConditions() {
        validateCondition();
        List<DataSqlCondition> conditions = new ArrayList<>();
        String[] resolverTime = resolverTime();
        List<BaseCondition.ValueMeta> resolverValue = resolverValue(this.getValue());
        if(CollectionUtil.isNotEmpty(resolverValue)) {
            resolverValue.forEach(meta -> {
                DataSqlCondition condition = new DataSqlCondition(this.timeType
                        , resolverTime[0], resolverTime[1]);
                if(meta.getType().equals("D")) {
                    conditions.add(condition.buildDevice(meta.getValue(), meta.getFields()));
                } else if(meta.getType().equals("P")) {
                    conditions.add(condition.buildProduct(meta.getValue(), meta.getFields()));
                } else {
                    conditions.add(condition.buildFields(meta.getFields()));
                }
            });
        } else {
            conditions.add(new DataSqlCondition(this.timeType, resolverTime[0], resolverTime[1]));
        }

        return conditions;
    }

    public List<CollectValueItem> buildResult(DataSqlCondition condition, List records, long total) {
        List<CollectValueItem> items = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(records)) {
            Map<String, List> collect = (Map<String, List>) records.stream()
                    .collect(Collectors.groupingBy(RealtimeCollectData::getField));
            condition.getFields().forEach(item -> {
                List values = collect.get(item);
                items.add(CollectValueItem.build(item, values, total));
            });
        } else {
            condition.getFields().forEach(item -> {
                items.add(new CollectValueItem(item, Collections.EMPTY_LIST));
            });
        }

        return items;
    }


    @Data
    public static class DataSqlCondition {
        private String uid;
        private String productCode;
        private List<String> fields;
        private String timeType;
        private String startTimestamp;
        private String endTimestamp;

        public DataSqlCondition(String uid, List<String> fields, TimeCountType timeType
                , String startTimestamp, String endTimestamp) {
            this.uid = uid;
            this.fields = fields;
            this.timeType = timeType.name();
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }

        public DataSqlCondition(TimeCountType timeType, String startTimestamp, String endTimestamp) {
            this.timeType = timeType.name();
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }

        public DataSqlCondition buildFields(List<String> fields) {
            this.fields = fields;
            return this;
        }

        public DataSqlCondition buildDevice(String uid, List<String> fields) {
            this.uid = uid;
            this.fields = fields;
            return this;
        }

        public DataSqlCondition buildProduct(String productCode, List<String> fields) {
            this.productCode = productCode;
            this.fields = fields;
            return this;
        }
    }
}
