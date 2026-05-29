package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.dto.CollectTaskDto;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.entity.CollectTask;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据采集任务 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-08-28
 */
public interface ICollectTaskService extends IBaseService<CollectTask> {

    Result<IPage<CollectTaskDto>> detailOfPage(Page<CollectTaskDto> page, CollectTaskDto entity);

    DetailResult<CollectTaskDto> detailById(Long id);

    CollectTaskDto collectDetailById(Long id);

    /**
     * 运行中的采集任务详情
     * @return
     */
    ListResult<CollectTaskDto> runningCollectTaskDetail();

    PageResult<Page<CollectDetail>> collectDetailPageById(Page page, Long id);

    Map<Long, List<CollectDevice>> listCollectDeviceByGroupIds(List<Long> groupIds);
}
