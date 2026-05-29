package com.iteaj.iboot.module.iot.controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.ParamMeta;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.file.UploadResult;
import com.iteaj.framework.spi.file.UploadService;
import com.iteaj.framework.spi.iot.protocol.ProtocolApiType;
import com.iteaj.framework.spi.iot.protocol.ProtocolModel;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.CtrlMode;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.iboot.module.iot.dto.ProtocolDto;
import com.iteaj.iboot.module.iot.entity.Product;
import com.iteaj.iboot.module.iot.entity.ProtocolApi;
import com.iteaj.iboot.module.iot.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.Protocol;
import com.iteaj.iboot.module.iot.service.IProtocolService;
import com.iteaj.framework.BaseController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报文协议管理
 * @author iteaj
 * @since 2023-09-12
 */
@RestController
@RequestMapping("/iot/protocol")
public class ProtocolController extends BaseController {

    private final UploadService uploadService;
    private final IProductService productService;
    private final IProtocolService protocolService;

    public ProtocolController(UploadService uploadService, IProductService productService, IProtocolService protocolService) {
        this.uploadService = uploadService;
        this.productService = productService;
        this.protocolService = protocolService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览报文协议功能")
    @CheckPermission({"iot:protocol:view"})
    public Result<IPage<ProtocolDto>> list(Page<Protocol> page, ProtocolDto entity) {
        return this.protocolService.pageDetail(page, entity);
    }

    /**
     * 获取协议列表
     * @return
     */
    @GetMapping("/list")
    public Result<List<Protocol>> list(Protocol entity) {
        return this.protocolService.list(entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:protocol:edit"})
    public Result<Protocol> getById(Long id) {
        return this.protocolService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新报文协议记录")
    @CheckPermission(value = {"iot:protocol:edit", "iot:protocol:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody Protocol entity) {
        entity.setUpdateTime(new Date());
        return this.protocolService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除报文协议记录")
    @CheckPermission({"iot:protocol:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.protocolService.removeByIds(idList);
    }

    /**
     * 导入Jar协议
     * @param file
     * @return
     */
    @Logger("导入Jar协议")
    @PostMapping("loadProtocol")
    public Result<Protocol> loadProtocol(MultipartFile file) throws IOException {
        // 先上传到临时文件
        UploadResult protocol = uploadService.upload(file.getInputStream()
                , file.getOriginalFilename(), file.getOriginalFilename(), ProtocolSupplierManager.PROTOCOL_JAR_TEMP_DIR);

        List<DeviceProtocolSupplier> load = ProtocolSupplierManager.loadFromTempProtocol(protocol.getFileName());
        if(load.size() > 1) {
            protocol.getFile().delete();
            return fail("jar文件只支持一个协议提供");
        } else if(load.size() == 0) {
            // jar包里面没有任何协议
            protocol.getFile().delete();
            return fail("未包含任务协议");
        }
        DeviceProtocolSupplier protocolSupplier = load.get(0);
        ProtocolModel protocolModel = protocolSupplier.getProtocol();
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(protocolModel.getCode());
        if(supplier != null) {
            if(supplier.getVersion().equals(protocolSupplier.getVersion())) {
                return fail("已存在相同版本的协议");
            }
        }

        ProtocolDto loadProtocol = this.protocolService.loadProtocol(protocolSupplier);
        loadProtocol.setJarPath(protocol.getFileName());
        Protocol result = new Protocol();
        BeanUtils.copyProperties(loadProtocol, result);
        return success(result, "加载成功");
    }

    /**
     * 获取协议详情
     * @param id
     * @return
     */
    @GetMapping("detail")
    public Result<ProtocolDto> detail(Long id) {
        return this.protocolService.getByDetail(id);
    }

    /**
     * 协议实现方式列表
     * @return
     */
    @GetMapping("implModes")
    public Result<List<IVOption>> implModes() {
        return success(ProtocolImplMode.options());
    }

    /**
     * 操控方式列表
     * @return
     */
    @GetMapping("ctrlModes")
    public Result<List<IVOption>> ctrlModes() {
        return success(CtrlMode.options());
    }

    /**
     * 获取协议对应的api列表
     * @param id 协议id
     * @param type 接口类型
     * @return
     */
    @GetMapping("apis")
    public Result<List<ProtocolApi>> apis(Long id, ProtocolApiType type) {
        List<ProtocolApi> protocolApis = this.protocolService.getByDetail(id).ofNullable().map(item -> {
            if (type == ProtocolApiType.func) {
                return item.getFuncApis();
            } else if (type == ProtocolApiType.event) {
                return item.getEventApis();
            } else {
                ArrayList<ProtocolApi> list = new ArrayList<>();
                if(!CollectionUtils.isEmpty(item.getFuncApis())) {
                    list.addAll(item.getFuncApis());
                }

                if(!CollectionUtils.isEmpty(item.getEventApis())) {
                    list.addAll(item.getEventApis());
                }
                return list;
            }
        }).orElseGet(ArrayList::new);
        return success(protocolApis);
    }

    /**
     * 系统内置协议列表
     * @return
     */
    @GetMapping("internals")
    public Result<List<IVOption>> internals() {
        List<IVOption> list = new ArrayList<>();
        Collection<DeviceProtocolSupplier> internals = ProtocolSupplierManager.internals();
        List<String> codes = internals.stream()
                .map(item -> item.getProtocol().getCode())
                .collect(Collectors.toList());

        Map<String, Protocol> protocolMap = protocolService.list(Wrappers.<Protocol>lambdaQuery().in(Protocol::getCode, codes))
                .stream().collect(Collectors.toMap(Protocol::getCode, item -> item));

        internals.forEach(item -> {
            Protocol protocol = protocolMap.get(item.getProtocol().getCode());
            if (protocol == null) {
                ProtocolDto protocolDto = protocolService.loadProtocol(item);
                protocol = new Protocol();
                BeanUtils.copyProperties(protocolDto, protocol);
            }

            list.add(new IVOption(item.getDesc(), item.getProtocol().getCode(), protocol).setDisabled(protocol.getId() != null));
        });
        return success(list);
    }

    /**
     * 获取产品对应协议的额外配置列表
     * @param productId 产品id
     * @param type 可选值(gateway、network)
     * @return
     */
    @GetMapping("listProtocolConfigByProductId")
    public Result<ParamMeta[]> listProtocolConfigMetas(Long productId, String type) {
        if(productId == null) {
            return fail("未指定参数[productId]");
        }

        Product product = this.productService.getById(productId)
                .ifNotPresentThrow("产品不存在").getData();
        return listProtocolConfigMetas(product.getProtocolCode(), type);
    }

    /**
     * 获取协议额外配置列表
     * @param protocolCode 协议代码
     * @param type 可选值(gateway、network)
     * @return
     */
    @GetMapping("listProtocolConfig")
    public Result<ParamMeta[]> listProtocolConfigMetas(String protocolCode, String type) {
        if(StrUtil.isBlank(type)) {
            return fail("未指定参数[type]");
        }
        if(StrUtil.isBlank(protocolCode)) {
            return fail("未指定参数[protocolCode]");
        }

        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(protocolCode);
        if(supplier == null) {
            return fail("找不到协议[protocolCode="+protocolCode+"]");
        }

        if(type.equals("network")) {
            return success(supplier.getNetworkConfig());
        } else if(type.equals("gateway")) {
            return success(supplier.getGatewayConfig());
        } else if(type.equals("child")) {
            return success(supplier.getDeviceConfig());
        } else {
            return fail("不支持的类型[type="+type+"]");
        }
    }
}

