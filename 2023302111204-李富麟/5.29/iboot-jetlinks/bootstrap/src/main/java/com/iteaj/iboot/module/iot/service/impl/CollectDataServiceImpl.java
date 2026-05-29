package com.iteaj.iboot.module.iot.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.consts.CollectSearchType;
import com.iteaj.framework.spi.iot.consts.TimeCountCondition;
import com.iteaj.framework.spi.iot.data.*;
import com.iteaj.iboot.module.iot.dto.CollectDataDto;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.mapper.CollectDataMapper;
import com.iteaj.iboot.module.iot.service.ICollectDataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectDataServiceImpl  extends BaseServiceImpl<CollectDataMapper, CollectData> implements ICollectDataService {

    @Override
    public PageResult detailOfPage(Page page, RealtimeCollectData entity) {
        return new PageResult<>(this.baseMapper.detailOfPage(page, entity));
    }

    @Override
    public List<EchartsCountValue> countOfCommonByTime(TimeCountCondition condition) {
        List<EchartsCountValue> values = new ArrayList<>();
        condition.buildSqlConditions().forEach(item -> {
            List<BaseEchartsCount> counts = this.getBaseMapper().countOfCommonByTime(item);
            List<EchartsCountValue> countValues = condition.buildResult(item, counts);
            values.addAll(countValues);
        });

        return values;
    }

    @Override
    public List<BaseEchartsCount> countLastWeek(Long deviceGroupId) {
        Map<String, BaseEchartsCount> map = new HashMap<>();
        List<BaseEchartsCount> dtos = this.getBaseMapper().countLastWeek(deviceGroupId);
        if(CollectionUtil.isNotEmpty(dtos)) {
            map = dtos.stream().collect(Collectors.toMap(BaseEchartsCount::getCategory, item -> item));
        }

        List<BaseEchartsCount> result = new ArrayList<>();
        DateTime lastWeek = new DateTime().offset(DateField.DAY_OF_MONTH, -7);
        for (int i = 0; i < 7; i++) {
            String time = lastWeek.toString("yyyy-MM-dd");
            BaseEchartsCount lastWeekDto = map.get(time);
            if(lastWeekDto != null) {
                result.add(lastWeekDto);
            } else {
                result.add(new BaseEchartsCount().buildZero(time));
            }

            lastWeek = lastWeek.offset(DateField.DAY_OF_MONTH, 1);
        }

        return result;
    }

    @Override
    public List<CollectValueItem> listOfCommonByTime(CollectDataCondition condition) {
        List<CollectValueItem> values = new ArrayList<>();
        if(condition.getSearchType() == CollectSearchType.list) {
            condition.buildSqlConditions().forEach(item -> {
                List<CollectDataDto> counts = this.getBaseMapper().listOfCommonByTime(item);
                List<CollectValueItem> valueItems = condition.buildResult(item, counts, 0);
                values.addAll(valueItems);
            });
        } else {
            condition.buildSqlConditions().forEach(item -> {
                if(item.getFields().size() > 1) {
                    throw new ServiceException("分页搜索不支持同时多个字段");
                }

                Page<CollectDataDto> page = new Page<>(condition.getCurrent(), condition.getSize());
                this.getBaseMapper().pageOfCommonByTime(page, item);
                List<CollectValueItem> valueItems = condition.buildResult(item, page.getRecords(), page.getTotal());
                values.addAll(valueItems);
            });
        }

        return values;
    }

    @Override
    public List<BaseEchartsCount> countLastMonth() {
        Map<String, BaseEchartsCount> map = new HashMap<>();
        List<BaseEchartsCount> dtos = this.getBaseMapper().countLastByDays(29);
        if(CollectionUtil.isNotEmpty(dtos)) {
            map = dtos.stream().collect(Collectors.toMap(BaseEchartsCount::getCategory, item -> item));
        }

        List<BaseEchartsCount> result = new ArrayList<>();
        DateTime lastWeek = new DateTime().offset(DateField.DAY_OF_MONTH, -29);
        for (int i = 0; i < 30; i++) {
            String time = lastWeek.toString("yyyy-MM-dd");
            BaseEchartsCount lastWeekDto = map.get(time);
            if(lastWeekDto != null) {
                result.add(lastWeekDto);
            } else {
                result.add(BaseEchartsCount.buildZero(time));
            }

            lastWeek = lastWeek.offset(DateField.DAY_OF_MONTH, 1);
        }

        return result;
    }
}
