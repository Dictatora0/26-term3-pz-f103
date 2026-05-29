package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.ProductType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 设备类型 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
public interface ProductTypeMapper extends BaseMapper<ProductType> {

    List<ProductType> listOfDetail(ProductType entity);

    IPage<ProductType> pageOfDetail(Page page, ProductType entity);
}
