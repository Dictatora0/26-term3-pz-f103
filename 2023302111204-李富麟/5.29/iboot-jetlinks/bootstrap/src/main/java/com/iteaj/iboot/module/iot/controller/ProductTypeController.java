package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.consts.DeviceTypeAlias;
import com.iteaj.iboot.module.iot.consts.TypeAliasOptions;
import com.iteaj.iboot.module.iot.dto.DebugTree;
import com.iteaj.iboot.module.iot.dto.ProductTree;
import com.iteaj.iboot.module.iot.service.IProductService;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.iboot.module.iot.entity.ProductType;
import com.iteaj.iboot.module.iot.service.IProductTypeService;
import com.iteaj.framework.BaseController;

/**
 * 产品类型管理
 *
 * @author iteaj
 * @since 2022-05-15
 */
@RestController
@RequestMapping("/iot/productType")
public class ProductTypeController extends BaseController {

    private final IProductService productService;
    private final IProductTypeService productTypeService;

    public ProductTypeController(IProductService productService, IProductTypeService productTypeService) {
        this.productService = productService;
        this.productTypeService = productTypeService;
    }

    /**
    * 分页列表查询
    */
    @GetMapping("/view")
    @CheckPermission({"iot:productType:view"})
    public Result<IPage<ProductType>> list(Page<ProductType> page, ProductType entity) {
        return this.productTypeService.pageOfDetail(page, entity);
    }

    /**
     * 树结构列表
     * @param entity
     * @return
     */
    @GetMapping("/tree")
    public Result<List<ProductType>> tree(ProductType entity) {
        return this.productTypeService.tree(entity);
    }

    /**
     * 产品类型-产品树
     * @param select 可选类型(productType,product) null都可选择
     * @return
     */
    @GetMapping("productTree")
    public Result<List<ProductTree>> productTree(String select) {
        // 获取所有产品类型
        Map<String, ProductTree> productTreeMap = productTypeService.list().stream()
                .map(item -> ProductTree.buildProductType(item.getId(), item.getPid(), item.getName()))
                .collect(Collectors.toMap(ProductTree::getKey, item -> item));

        // 构建产品类型树
        productTreeMap.values().forEach(item -> {
            ProductTree parent = productTreeMap.get(item.getPkey());
            if(parent != null) {
                parent.addChildren(item);
            }

            // 只有产品可以选中
            if(Objects.equals(select, "product")) {
                item.setDisabled(true);
            }
        });

        // 将产品放入对应的产品类型
        productService.list().forEach(item -> {
            if(item.getProductTypeId() != null) {
                ProductTree parent = productTreeMap.get("productType:" + item.getProductTypeId());
                if(parent != null) {
                    ProductTree product = ProductTree.buildProduct(item.getId(), parent.getId(), item.getName());
                    parent.addChildren(product);

                    // 只有产品类型可以选中
                    if(Objects.equals(select, "productType")) {
                        product.setDisabled(true);
                    }
                }
            }
        });

        return success(productTreeMap.values().stream()
                .filter(item -> Objects.equals(item.getPkey(), "productType:0"))
                .collect(Collectors.toList()));
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:productType:edit"})
    public Result<ProductType> getEditDetail(Long id) {
        return this.productTypeService.getById(id);
    }

    /**
    * 修改记录
    * @param entity
    */
    @PostMapping("/edit")
    @CheckPermission({"iot:productType:edit"})
    public Result<Boolean> edit(@RequestBody ProductType entity) {
        return this.productTypeService.updateById(entity);
    }

    /**
    * 新增记录
    * @param entity
    */
    @PostMapping("/add")
    @CheckPermission({"iot:productType:add"})
    public Result<Boolean> add(@RequestBody ProductType entity) {
        return this.productTypeService.save(entity);
    }

    /**
     * 新增或者修改
     * @param entity
     */
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:productType:add", "iot:productType:edit"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ProductType entity) {
        return this.productTypeService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @CheckPermission({"iot:productType:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.productTypeService.removeByIds(idList);
    }

    /**
     * 获取设备类型别名列表
     * @return
     */
    @GetMapping("alias")
    public Result<List<TypeAliasOptions>> alias() {
        return success(DeviceTypeAlias.options());
    }
}

