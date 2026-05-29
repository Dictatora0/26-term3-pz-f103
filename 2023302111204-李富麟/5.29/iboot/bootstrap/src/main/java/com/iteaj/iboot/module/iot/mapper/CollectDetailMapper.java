package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.CollectDetail;

/**
 * <p>
 * 数据采集任务 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-08-28
 */
public interface CollectDetailMapper extends BaseMapper<CollectDetail> {

    Page<CollectDetail> detailPage(Page page, CollectDetail entity);

    CollectDetail detailById(Long id);

    /**
     * 采集任务是否在运行
     * @param collectTaskId
     * @return
     */
    boolean hasTaskRunning(Long collectTaskId);
}
