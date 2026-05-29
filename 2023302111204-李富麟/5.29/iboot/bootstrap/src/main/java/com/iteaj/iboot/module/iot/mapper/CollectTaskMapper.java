package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.collect.CollectDevice;
import com.iteaj.iboot.module.iot.collect.CollectSignal;
import com.iteaj.iboot.module.iot.dto.CollectTaskDto;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.entity.CollectTask;
import com.iteaj.iboot.module.iot.entity.Signal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 数据采集任务 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-08-28
 */
public interface CollectTaskMapper extends BaseMapper<CollectTask> {

    IPage<CollectTaskDto> detailOfPage(Page<CollectTaskDto> page, CollectTaskDto entity);

    CollectTaskDto detailById(Long id);

    CollectTaskDto collectDetailById(Long id);

    Page<CollectDetail> collectDetailPageById(Page page, Long id);

    /**
     * 获取运行中的采集任务
     * @return
     */
    List<CollectTaskDto> listRunningCollectTaskDetail();

    List<CollectDevice> listCollectDeviceByGroupIds(List<Long> groupIds);

    List<CollectSignal> listCollectSignalByGroupIds(List<Long> groupIds);

    /**
     * 移除任务和对应的任务详情
     * @param idList
     * @return
     */
    void removeTaskAndDetail(Collection<? extends Serializable> idList);
}
