package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.IBaseService;
import com.iteaj.iboot.module.iot.entity.Signal;

import java.util.List;

/**
 * <p>
 * 寄存器点位 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
public interface ISignalService extends IBaseService<Signal> {

    Result<IPage<Signal>> detailByPage(Page<Signal> page, Signal entity);

    ListResult<Signal> listByProductIds(List<Long> productIds, String name);

    DetailResult<Signal> detailById(Long id);
}
