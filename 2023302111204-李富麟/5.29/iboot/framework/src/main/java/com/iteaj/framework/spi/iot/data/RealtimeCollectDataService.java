package com.iteaj.framework.spi.iot.data;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.consts.TimeCountCondition;

import java.util.*;

public interface RealtimeCollectDataService {

    PageResult detailOfPage(Page page, RealtimeCollectData entity);

    /**
     * 通过条件统计
     * @param condition
     * @return
     */
    List<EchartsCountValue> countOfCommonByTime(TimeCountCondition condition);

    /**
     * 统计最近一个月的数据
     * @return
     */
    List<BaseEchartsCount> countLastMonth();

    /**
     * 统计最近一周的数据
     * @param deviceGroupId
     * @return
     */
    List<BaseEchartsCount> countLastWeek(Long deviceGroupId);

    /**
     * 根据条件获取实时值
     * @see CollectDataCondition#getTimeType()
     * @param condition
     * @return
     */
    List<CollectValueItem> listOfCommonByTime(CollectDataCondition condition);
}
