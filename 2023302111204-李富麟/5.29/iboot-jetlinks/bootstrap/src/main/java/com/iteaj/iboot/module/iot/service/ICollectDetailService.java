package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.entity.CollectDetail;

public interface ICollectDetailService extends IBaseService<CollectDetail> {

    PageResult<Page<CollectDetail>> detailPage(Page page, CollectDetail entity);

    DetailResult<CollectDetail> detailById(Long id);

    boolean hasTaskRunning(Long collectTaskId);
}
