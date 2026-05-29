package com.iteaj.framework.spi.iot.consts;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.data.BaseCondition;
import com.iteaj.framework.spi.iot.data.BaseEchartsCount;
import com.iteaj.framework.spi.iot.data.EchartsCountValue;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 时间统计条件
 */
@Getter
@Setter
public class TimeCountCondition extends BaseCondition{

    /**
     * 计算类型
     */
    private CountCalcType calcType = CountCalcType.count;

    private List<BaseEchartsCount> timeEmptyValue = new ArrayList<>();

    public List<TimeCountSqlCondition> buildSqlConditions() {
        validateCondition();
        List<TimeCountSqlCondition> conditions = new ArrayList<>();
        String[] resolverTime = resolverTime();
        List<BaseCondition.ValueMeta> resolverValue = resolverValue(this.getValue());
        if(CollectionUtil.isNotEmpty(resolverValue)) {
            resolverValue.forEach(meta -> {
                TimeCountSqlCondition condition = new TimeCountSqlCondition(this.getTimeType()
                        , this.calcType, resolverTime[0], resolverTime[1]);
                if(meta.getType().equals("D")) {
                    conditions.add(condition.buildDevice(meta.getValue(), meta.getFields()));
                } else if(meta.getType().equals("P")) {
                    conditions.add(condition.buildProduct(meta.getValue(), meta.getFields()));
                } else {
                    conditions.add(condition.buildFields(meta.getFields()));
                }
            });
        } else {
            conditions.add(new TimeCountSqlCondition(this.getTimeType(), this.calcType, resolverTime[0], resolverTime[1]));
        }

        return conditions;
    }

    public String[] resolverTime() {
        TimeCountCondition condition = this;
        String[] result = new String[]{condition.getTimeRange().get(0), condition.getTimeRange().get(1)};
        TimeCountType timeType = condition.getTimeType();
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
            DateTime tempDate = startDateTime;
            while (tempDate.getTime() <= (endDateTime.getTime() - 1)) {
                String time = tempDate.toString("yyyy");
                timeEmptyValue.add(new BaseEchartsCount(time, 0));
                tempDate = tempDate.offset(DateField.YEAR, 1);
            }
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
            DateTime tempDate = startDateTime;
            while (tempDate.getTime() <= (endDateTime.getTime() - 1)) {
                String time = tempDate.toString("yyyy-MM");
                timeEmptyValue.add(new BaseEchartsCount(time, 0));
                tempDate = tempDate.offset(DateField.MONTH, 1);
            }
        } else if(timeType == TimeCountType.week) {
            DateTime startDateTime = startTime.offset(DateField.WEEK_OF_MONTH, startOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.WEEK_OF_MONTH, endOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
            DateTime tempDate = startDateTime;
            while (tempDate.getTime() <= (endDateTime.getTime() - 1)) {
                String time = tempDate.toString("yyyy-MM-dd");
                timeEmptyValue.add(new BaseEchartsCount(time, 0));
                tempDate = tempDate.offset(DateField.DAY_OF_MONTH, 1);
            }
        } else if(timeType == TimeCountType.day) {
            DateTime startDateTime = startTime.offset(DateField.DAY_OF_MONTH, startOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDateTime = endTime.offset(DateField.DAY_OF_MONTH, endOffset)
                    .setField(DateField.HOUR_OF_DAY, 0).setField(DateField.MINUTE, 0)
                    .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDateTime.getTime() - 1);
            DateTime tempDate = startDateTime;
            while (tempDate.getTime() <= (endDateTime.getTime() - 1)) {
                String time = tempDate.toString("yyyy-MM-dd");
                timeEmptyValue.add(new BaseEchartsCount(time, 0));
                tempDate = tempDate.offset(DateField.DAY_OF_MONTH, 1);
            }
        } else if(timeType == TimeCountType.hour) {
            DateTime startDateTime = startTime.offset(DateField.HOUR_OF_DAY, startOffset)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[0] = "" + startDateTime.getTime();
            DateTime endDatetime = endTime.offset(DateField.HOUR_OF_DAY, endOffset)
                    .setField(DateField.MINUTE, 0).setField(DateField.SECOND, 0)
                    .setField(DateField.MILLISECOND, 0);
            result[1] = "" + (endDatetime.getTime() - 1);
            DateTime tempDate = startDateTime;
            while (tempDate.getTime() <= (endDatetime.getTime() - 1)) {
                String time = tempDate.toString("yy-MM-dd HH");
                timeEmptyValue.add(new BaseEchartsCount(time, 0));
                tempDate = tempDate.offset(DateField.HOUR_OF_DAY, 1);
            }
        } else {
            throw new ServiceException("不支持的时间类型["+condition.getTimeType()+"]");
        }

        return result;
    }

    public List<EchartsCountValue> buildResult(TimeCountSqlCondition condition, List<BaseEchartsCount> counts) {
        List<EchartsCountValue> result = new ArrayList<>();
        if(CollectionUtil.isEmpty(counts)) {
            if(CollectionUtil.isNotEmpty(condition.fields)) {
                condition.fields.forEach(field -> {
                    result.add(new EchartsCountValue(condition.uid, field, this.timeEmptyValue));
                });
            } else {
                result.add(new EchartsCountValue(condition.uid, this.timeEmptyValue));
            }
        } else {
            if(CollectionUtil.isNotEmpty(condition.fields)) {
                Map<String, List<BaseEchartsCount>> fieldGroupCount = counts.stream()
                        .collect(Collectors.groupingBy(BaseEchartsCount::getField));
                condition.fields.forEach(item -> {
                    List<BaseEchartsCount> baseEchartsCounts = fieldGroupCount.get(item);
                    if(baseEchartsCounts == null) {
                        result.add(new EchartsCountValue(condition.uid, item, this.timeEmptyValue));
                    } else {
                        Map<String, BaseEchartsCount> timeCountMap = baseEchartsCounts.stream()
                                .collect(Collectors.toMap(item1 -> item1.getCategory(), item1 -> item1));
                        for (int i = 0; i <timeEmptyValue.size(); i++) {
                            BaseEchartsCount emptyTimeCount = timeEmptyValue.get(i);
                            if(!timeCountMap.containsKey(emptyTimeCount.getCategory())) {
                                baseEchartsCounts.add(i, emptyTimeCount);
                            }
                        }
                        result.add(new EchartsCountValue(condition.uid, item, baseEchartsCounts));
                    }
                });
            } else {
                Map<String, BaseEchartsCount> timeCountMap = counts.stream()
                        .collect(Collectors.toMap(item -> item.getCategory(), item -> item));
                for (int i = 0; i <timeEmptyValue.size(); i++) {
                    BaseEchartsCount emptyTimeCount = timeEmptyValue.get(i);
                    if(!timeCountMap.containsKey(emptyTimeCount.getCategory())) {
                        counts.add(i, emptyTimeCount);
                    }
                }

                result.add(new EchartsCountValue(condition.uid, null, counts));
            }
        }

        return result;
    }

    @Data
    public static class TimeCountSqlCondition {
        private String uid;
        private String productCode;
        private List<String> fields;
        private String timeType;
        private String calcType;
        private String startTimestamp;
        private String endTimestamp;

        public TimeCountSqlCondition(String uid, List<String> fields, TimeCountType timeType
                , CountCalcType calcType, String startTimestamp, String endTimestamp) {
            this.uid = uid;
            this.fields = fields;
            this.timeType = timeType.name();
            this.calcType = calcType.name();
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }

        public TimeCountSqlCondition(TimeCountType timeType, CountCalcType calcType, String startTimestamp, String endTimestamp) {
            this.timeType = timeType.name();
            this.calcType = calcType.name();
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }

        public TimeCountSqlCondition buildFields(List<String> fields) {
            this.fields = fields;
            return this;
        }

        public TimeCountSqlCondition buildDevice(String uid, List<String> fields) {
            this.uid = uid;
            this.fields = fields;
            return this;
        }

        public TimeCountSqlCondition buildProduct(String productCode, List<String> fields) {
            this.productCode = productCode;
            this.fields = fields;
            return this;
        }
    }
}
