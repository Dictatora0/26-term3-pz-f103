package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.iot.data.BaseEchartsCount;
import com.iteaj.framework.spi.iot.data.RealtimeCollectData;
import com.iteaj.framework.spi.iot.data.RealtimeCollectDataService;
import com.iteaj.iboot.module.iot.entity.CollectData;

import java.util.List;

public interface ICollectDataService extends IBaseService<CollectData>, RealtimeCollectDataService {

    PageResult detailOfPage(Page page, RealtimeCollectData entity);

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
}
