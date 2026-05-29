package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.spi.iot.consts.TimeCountCondition;
import com.iteaj.framework.spi.iot.data.BaseEchartsCount;
import com.iteaj.framework.spi.iot.data.CollectDataCondition;
import com.iteaj.framework.spi.iot.data.CollectTimeValue;
import com.iteaj.framework.spi.iot.data.RealtimeCollectData;
import com.iteaj.iboot.module.iot.dto.CollectDataDto;
import com.iteaj.iboot.module.iot.entity.CollectData;

import java.util.List;

public interface CollectDataMapper extends BaseMapper<CollectData> {

    Page<CollectDataDto> detailOfPage(Page page, RealtimeCollectData entity);

    /**
     * 统计最近一周的数据
     * @param deviceGroupId
     * @return
     */
    List<BaseEchartsCount> countLastWeek(Long deviceGroupId);

    /**
     * 统计最近指定天的数据
     * @param days 天数
     * @return
     */
    List<BaseEchartsCount> countLastByDays(int days);

    /**
     * 通用的时间统计
     * @return
     * @param item
     */
    List<BaseEchartsCount> countOfCommonByTime(TimeCountCondition.TimeCountSqlCondition item);

    /**
     * 通用的时间列表
     * @param item
     * @return
     */
    List<CollectDataDto> listOfCommonByTime(CollectDataCondition.DataSqlCondition item);

    Page pageOfCommonByTime(Page<CollectDataDto> page, CollectDataCondition.DataSqlCondition condition);
}
