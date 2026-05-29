package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.iot.*;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.dto.ProductDto;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品管理
 *
 * @author iteaj
 * @since 2022-07-22
 */
@RestController
@RequestMapping("/iot/product")
public class ProductController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;

    public ProductController(IDeviceService deviceService, IProductService productService) {
        this.deviceService = deviceService;
        this.productService = productService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @CheckPermission({"iot:product:view"})
    public Result<IPage<ProductDto>> list(Page<Product> page, Product entity) {
        return this.productService.pageOfDetail(page, entity);
    }

    /**
     * 返回产品列表
     * @param entity
     * @return
     */
    @GetMapping("/list")
    public Result<List<IVOption>> list(Product entity) {
        List<IVOption> options = this.productService.list(entity).stream()
                .map(item -> new IVOption(item.getName(), item.getId()))
                .collect(Collectors.toList());
        return success(options);
    }

    /**
     * 获取指定类型下面的列表
     * @param gatewayId 网关id
     * @param deviceType 设备类型
     * @param productTypeId -1将获取所有
     * @return
     */
    @GetMapping("/listByType")
    public Result<List<ProductDto>> listByType(Long productTypeId, DeviceType deviceType, Long gatewayId) {
        Product product = new Product();
        product.setGatewayId(gatewayId);
        product.setDeviceType(deviceType);
        product.setProductTypeId(productTypeId);
        return this.productService.listOfDetail(product);
    }

    /**
     * 父产品列表
     * @return
     */
    @GetMapping("parent")
    public Result<List<Product>> parent() {
        return this.productService.list(Wrappers.<Product>lambdaQuery()
                .eq(Product::getDeviceType, DeviceType.Gateway));
    }


    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:product:edit"})
    public Result<Product> getById(Long id) {
        return this.productService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @Logger("保存或更新产品")
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:product:edit", "iot:product:add"}, logical = Logical.OR)
    public Result<Boolean> save(@Validated @RequestBody Product entity) {
        return this.productService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @Logger("删除产品")
    @PostMapping("/del")
    @CheckPermission({"iot:product:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.productService.removeByIds(idList);
    }

    /**
     * 设备类型列表
     * @return
     */
    @GetMapping("deviceTypes")
    public Result<List<IVOption>> deviceTypes() {
        return success(DeviceType.options());
    }

    /**
     * 切换产品状态
     * @param id 产品id
     * @param status 状态
     * @return
     */
    @Logger("切换产品状态")
    @PostMapping("switch/{id}/{status}")
    @CheckPermission({"iot:gateway:switch"})
    public Result<Boolean> switchStatus(@PathVariable Long id,  @PathVariable FuncStatus status) {
        this.productService.switchStatus(id, status);
        return success(true);
    }

    /**
     * 获取点位协议的产品列表
     * @return
     */
    @GetMapping("listByPoint")
    public Result<List<Product>> listByPoint() {
        return this.productService.listByPoint();
    }

    /**
     * 解析器列表
     * @return
     */
    @GetMapping("resolvers")
    public Result<List<IVOption>> resolvers() {
        return success(DataValueResolverFactory.options());
    }
}

