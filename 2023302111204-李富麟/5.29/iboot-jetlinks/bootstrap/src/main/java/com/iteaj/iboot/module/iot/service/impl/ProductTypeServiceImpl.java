package com.iteaj.iboot.module.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.NumberResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.utils.TreeUtils;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.entity.ProductType;
import com.iteaj.iboot.module.iot.mapper.ProductTypeMapper;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iboot.module.iot.service.IProductTypeService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备类型 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2022-05-15
 */
@Service
public class ProductTypeServiceImpl extends BaseServiceImpl<ProductTypeMapper, ProductType> implements IProductTypeService {

    @Autowired
    private IProductService productService;

    @Override
    public PageResult<IPage<ProductType>> pageOfDetail(Page page, ProductType entity) {
        return new PageResult<>(this.getBaseMapper().pageOfDetail(page, entity));
    }

    @Override
    public ListResult<ProductType> listOfDetail(ProductType entity) {
        return new ListResult(this.getBaseMapper().listOfDetail(entity));
    }

    @Override
    public ListResult<ProductType> tree(ProductType entity) {
        List<ProductType> types = this.listOfDetail(entity)
                .ofNullable()
                .map(value -> TreeUtils.toTrees(value).stream().collect(Collectors.toList()))
                .get();
        return new ListResult<>(types);
    }

    @Override
    @Transactional
    public BooleanResult save(ProductType entity) {
        this.getOne(Wrappers.<ProductType>lambdaQuery().eq(ProductType::getName, entity.getName()))
                .ifPresentThrow("产品类型名称已存在["+entity.getName()+"]");

        super.save(entity);
        if(entity.getPid() != null && entity.getPid() != 0) {
            ProductType parent = this.getById(entity.getPid())
                    .ifNotPresentThrow("父类型不存在")
                    .getData();
            entity.setPath(parent.getPath()+","+entity.getId());
        } else {
            entity.setPath(entity.getId().toString());
        }

        return this.update(Wrappers.<ProductType>lambdaUpdate()
                .set(ProductType::getPath, entity.getPath())
                .eq(ProductType::getId, entity.getId()));
    }

    @Override
    @Transactional
    public BooleanResult updateById(ProductType entity) {
        this.getOne(Wrappers.<ProductType>lambdaQuery()
                .eq(ProductType::getName, entity.getName())
                .ne(ProductType::getId, entity.getId()))
                .ifPresentThrow("产品类型名称已存在["+entity.getName()+"]");
        if(entity.getPid() != null && entity.getPid() != 0) {
            ProductType parent = this.getById(entity.getPid())
                    .ifNotPresentThrow("父类型不存在")
                    .getData();

            entity.setPath(parent.getPath()+","+entity.getId());
        } else {
            entity.setPath(entity.getId().toString());
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isEmpty(idList)) {
            return new BooleanResult(false);
        }

        this.list(Wrappers.<ProductType>lambdaQuery().in(ProductType::getPid, idList)).ifPresent(item -> {
            throw new ServiceException("请先删除对应的子类型");
        });

        NumberResult<Integer> count = productService.count(Wrappers
                .<Product>lambdaQuery().in(Product::getProductTypeId, idList));
        Integer integer = count.ofNullable().orElse(0);
        if(integer > 0) {
            throw new ServiceException("此类型已在使用");
        }

        return super.removeByIds(idList);
    }
}
