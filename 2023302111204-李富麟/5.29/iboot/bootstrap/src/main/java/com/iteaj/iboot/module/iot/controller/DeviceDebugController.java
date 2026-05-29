package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.consts.DeviceType;
import com.iteaj.iboot.module.iot.dto.DebugDto;
import com.iteaj.iboot.module.iot.dto.DebugTree;
import com.iteaj.iboot.module.iot.entity.Device;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import com.iteaj.iboot.module.iot.service.IModelApiService;
import com.iteaj.iboot.module.iot.service.IProductService;
import com.iteaj.iboot.module.iot.service.IProductTypeService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备调试管理
 */
@RestController
@RequestMapping("/iot/debug")
public class DeviceDebugController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;
    private final IModelApiService modelApiService;
    private final IProductTypeService productTypeService;

    public DeviceDebugController(IDeviceService deviceService
            , IProductService productService, IModelApiService modelApiService
            , IProductTypeService productTypeService) {
        this.deviceService = deviceService;
        this.productService = productService;
        this.modelApiService = modelApiService;
        this.productTypeService = productTypeService;
    }

    /**
     * 产品类型、产品、设备树
     * @param productId 产品id
     * @return
     */
    @GetMapping("/tree")
    public Result<List<DebugTree>> tree(Long productId) {
        if(productId == null) {
            // 获取所有产品类型
            Map<String, DebugTree> debugTreeMap = productTypeService.list().stream()
                    .map(item -> DebugTree.buildProductType(item.getId(), item.getPid(), item.getName()))
                    .collect(Collectors.toMap(DebugTree::getKey, item -> item));

            // 构建产品类型树
            debugTreeMap.values().forEach(item -> {
                DebugTree parent = debugTreeMap.get(item.getPkey());
                if(parent != null) {
                    parent.addChildren(item);
                }
            });

            // 将产品放入对应的产品类型
            productService.list(Wrappers.<Product>lambdaQuery()
                    .ne(Product::getDeviceType, DeviceType.Gateway))
                    .forEach(item -> {
                        if(item.getProductTypeId() != null) {
                            DebugTree parent = debugTreeMap.get("productType:" + item.getProductTypeId());
                            if(parent != null) {
                                parent.addChildren(DebugTree.buildProduct(item.getId(), parent.getId(), item.getName()));
                            }
                        }
                    });

            debugTreeMap.values().forEach(item -> {
                if(!item.isProduct()) {
                    item.setChildren(null);

                    final DebugTree parent = item.getParent();
                    if(parent != null && parent.getChildren() != null) {
                        parent.getChildren().remove(item);
                    }
                }

            });

            return success(debugTreeMap.values().stream()
                    .filter(item -> Objects.equals(item.getPkey(), "productType:0"))
                    .collect(Collectors.toList()));
        } else {
            List<DebugTree> debugTrees = deviceService.list(Wrappers.<Device>lambdaQuery()
                    .eq(Device::getProductId, productId))
                    .stream()
                    .map(item -> DebugTree.buildDevice(item.getId(), productId, item.getName(), item.getStatus()))
                    .collect(Collectors.toList());
            return success(debugTrees);
        }
    }

    /**
     * 调试详情
     * @param deviceId
     * @return
     */
    @GetMapping("detail")
    public Result<DebugDto> detail(Long deviceId) {
        DebugDto debugDto = new DebugDto();
        deviceService.detailById(deviceId)
            .ifNotPresentThrow("设备不存在").of().ifPresent(item -> {
                debugDto.setDevice(item);
            productService.debugById(item.getProductId())
                .ifNotPresentThrow("此设备数据不完整["+item.getName()+"]")
                .ifPresent(productDto -> {
                    debugDto.setProduct(productDto);
                    if(!CollectionUtils.isEmpty(productDto.getFuncApis())) {
                        productDto.getFuncApis().forEach(api -> debugDto.addApiDebugParam(api.getCode(), api.resolveDownApiConfig(productDto.getAttrs())));
                    }
                    productDto.setAttrs(null);
                });
        });

        return success(debugDto);
    }
}
