package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.entity.PointGroup;
import com.iteaj.framework.IBaseService;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 点位组 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
public interface IPointGroupService extends IBaseService<PointGroup> {

    /**
     * 返回页面详情信息
     * @param page
     * @param entity
     * @return
     */
    PageResult<IPage<PointGroup>> detailOfPage(Page<PointGroup> page, PointGroup entity);

    /**
     * 获取详情信息
     * @param id
     * @return
     */
    DetailResult<PointGroup> detailById(Long id);

    /**
     * 是否有被点位组使用
     * @param list
     * @return
     */
    Boolean isBindSignals(Collection<? extends Serializable> list);

    /**
     * 是否被采集任务绑定
     * @param idList
     * @return
     */
    boolean isBindCollectTask(Collection<? extends Serializable> idList);
}
