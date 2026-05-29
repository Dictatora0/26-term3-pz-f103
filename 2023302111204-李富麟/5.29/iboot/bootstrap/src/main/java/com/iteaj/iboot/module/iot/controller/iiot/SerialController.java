package com.iteaj.iboot.module.iot.controller.iiot;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.collect.CollectOption;
import com.iteaj.iboot.module.iot.consts.SerialStatus;
import com.iteaj.iboot.module.iot.entity.Serial;
import com.iteaj.iboot.module.iot.service.ISerialService;
import com.iteaj.iot.serial.SerialClient;
import com.iteaj.iot.serial.SerialComponent;
import com.iteaj.iot.serial.SerialConnectProperties;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 串口设备管理
 *
 * @author iteaj
 * @since 2023-04-12
 */
@RestController
@RequestMapping("/iot/serial")
public class SerialController extends BaseController {

    private final ISerialService serialService;

    public SerialController(ISerialService serialService) {
        this.serialService = serialService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @CheckPermission({"iot:serial:view"})
    public Result<IPage<Serial>> list(Page<Serial> page, Serial entity) {
        return this.serialService.page(page, entity);
    }

    /**
     * 列表查询
     * @param entity 搜索条件
     */
    @GetMapping("/list")
    public Result<List<Serial>> list(Serial entity) {
        return this.serialService.list(entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:serial:edit"})
    public Result<Serial> getById(Long id) {
        return this.serialService.getById(id);
    }


    /**
     * 获取记录
     * @param com 串口
     */
    @GetMapping("/getByCom")
    public Result<Serial> getByCom(String com) {
        return this.serialService.getByCom(com);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"iot:serial:edit", "iot:serial:add"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody Serial entity) {
        return this.serialService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @CheckPermission({"iot:serial:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.serialService.removeByIds(idList);
    }

    /**
     * 可用的串口列表
     * @return
     */
    @GetMapping("/available")
    public Result<List<CollectOption>> available() {
        List<CollectOption> collect = Arrays.stream(SerialComponent.instance().available())
                .map(item -> {
                    String portName = item.getSystemPortName();
                    return new CollectOption(portName, portName);
                })
                .collect(Collectors.toList());
        return success(collect);
    }

    @PostMapping("connect/{status}")
    @CheckPermission({"iot:serial:connect"})
    public Result<Boolean> connect(@RequestBody Serial device, @PathVariable SerialStatus status) {
        SerialClient client = SerialComponent.instance().getClient(device.getCom());
        if(status == SerialStatus.open) {
            if(client == null) {
                SerialConnectProperties config = new SerialConnectProperties(device.getCom(), device.getBaudRate())
                        .config(device.getDataBits(), device.getStopBits(), device.getParity());
                client = SerialComponent.instance().createNewClientAndConnect(config);
            }

            if(client.isOpen()) {
                serialService.update(Wrappers.<Serial>lambdaUpdate()
                        .set(Serial::getStatus, status)
                        .eq(Serial::getId, device.getId()));
                return success("打开串口成功");
            } else {
                return fail("串口未挂载或被暂用");
            }
        } else {
            if(client != null && client.isOpen()) {
                serialService.update(Wrappers.<Serial>lambdaUpdate()
                        .set(Serial::getStatus, status)
                        .eq(Serial::getId, device.getId()));
                return success(client.disconnect());
            } else {
                serialService.update(Wrappers.<Serial>lambdaUpdate()
                        .set(Serial::getStatus, status)
                        .eq(Serial::getId, device.getId()));
                return success("关闭串口成功");
            }
        }
    }
}

