package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.iot.DeviceProtocolSupplier;
import com.iteaj.framework.spi.iot.ProtocolSupplierManager;
import com.iteaj.framework.spi.iot.consts.ConnectionType;
import com.iteaj.framework.spi.iot.consts.GatewayType;
import com.iteaj.framework.spi.iot.consts.TransportProtocol;
import com.iteaj.iboot.module.iot.consts.GatewayStatus;
import com.iteaj.iboot.module.iot.entity.Gateway;
import com.iteaj.iboot.module.iot.service.IGatewayService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 协议网关管理
 * @author iteaj
 * @since 2023-09-12
 */
@RestController
@RequestMapping("/iot/gateway")
public class GatewayController extends BaseController {

    private final IGatewayService gatewayService;

    public GatewayController(IGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览协议网关功能")
    @CheckPermission({"iot:gateway:view"})
    public Result<IPage<Gateway>> list(Page<Gateway> page, Gateway entity) {
        return this.gatewayService.page(page.addOrder(OrderItem.desc("create_time")), entity);
    }

    /**
     * 获取网关列表
     * @param entity
     * @return
     */
    @GetMapping("list")
    public Result<List<Gateway>> list(Gateway entity) {
        return this.gatewayService.list(entity);
    }

    /**
     * 协议列表
     * @param type 协议类型
     * @return
     */
    @GetMapping("protocols")
    public Result<List<IVOption>> protocols(GatewayType type, TransportProtocol protocolType, ConnectionType connectionType) {
        return gatewayService.protocols(type, protocolType, connectionType).forEach(item -> {
            DeviceProtocolSupplier supplier = ProtocolSupplierManager.get((String) item.getExtra());
            if(supplier != null) {
                item.addConfig("config", supplier.getNetworkConfig());
            } else {
                item.addConfig("bind", "不可用");
            }
        });
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:gateway:edit"})
    public Result<Gateway> getById(Long id) {
        return this.gatewayService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新协议网关记录")
    @CheckPermission(value = {"iot:gateway:edit", "iot:gateway:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@Validated @RequestBody Gateway entity) {
        return this.gatewayService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除协议网关记录")
    @CheckPermission({"iot:gateway:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.gatewayService.removeByIds(idList);
    }

    /**
     * 传输协议列表
     * @return
     */
    @GetMapping("transportProtocol")
    public Result<List<IVOption>> transportProtocol() {
        return success(TransportProtocol.options());
    }

    /**
     * 切换网关状态
     * @param id 网关id
     * @param status 状态
     * @return
     */
    @Logger("切换网关状态")
    @PostMapping("switch/{id}/{status}")
    @CheckPermission({"iot:gateway:switch"})
    public Result<Boolean> switchStatus(@PathVariable Long id,  @PathVariable GatewayStatus status) {
        this.gatewayService.switchStatus(id, status);
        return success(true);
    }
}

