package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.GroupPoint;
import com.iteaj.iboot.module.iot.entity.PointGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 点位组 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
public interface PointGroupMapper extends BaseMapper<PointGroup> {

    /**
     * 获取详情的分页信息
     * @param page
     * @param entity
     * @return
     */
    IPage<PointGroup> detailOfPage(Page<PointGroup> page, PointGroup entity);

    /**
     * 返回详情记录
     * @param id
     * @return
     */
    PointGroup detailById(Long id);

    /**
     * 保存组点位列表
     * @param list
     */
    void batchSaveGroupPoint(List<GroupPoint> list);

    /**
     * 是否绑定信号
     * @param list
     * @return
     */
    Boolean isBindSignals(Collection<? extends Serializable> list);

    /**
     * 是否被采集任务绑定
     * @param idList 点位组id列表
     * @return
     */
    boolean isBindCollectTask(Collection<? extends Serializable> idList);
}
