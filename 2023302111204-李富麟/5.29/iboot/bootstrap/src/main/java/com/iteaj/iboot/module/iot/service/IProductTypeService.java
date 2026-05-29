package com.iteaj.iboot.module.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.iot.entity.ProductType;
import com.iteaj.framework.IBaseService;

/**
 * <p>
 * 设备类型 服务类
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
public interface IProductTypeService extends IBaseService<ProductType> {

    PageResult<IPage<ProductType>> pageOfDetail(Page page, ProductType entity);

    ListResult<ProductType> listOfDetail(ProductType entity);

    /**
     * 获取树形结构列表
     * @return
     */
    ListResult<ProductType> tree(ProductType entity);
}
