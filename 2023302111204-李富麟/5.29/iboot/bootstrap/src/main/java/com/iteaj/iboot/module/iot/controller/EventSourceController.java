package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.collect.model.EventSourceCollectService;
import com.iteaj.iboot.module.iot.consts.EventSourceType;
import com.iteaj.iboot.module.iot.consts.FuncStatus;
import com.iteaj.iboot.module.iot.entity.DeviceGroup;
import com.iteaj.iboot.module.iot.service.IDeviceGroupService;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.EventSource;
import com.iteaj.iboot.module.iot.service.IEventSourceService;
import com.iteaj.framework.BaseController;

/**
 * 事件源管理
 * @author iteaj
 * @since 2024-02-27
 */
@RestController
@RequestMapping("/iot/eventSource")
public class EventSourceController extends BaseController {

    private final IDeviceGroupService deviceGroupService;
    private final IEventSourceService eventSourceService;
    private final EventSourceCollectService eventSourceCollectService;
    public EventSourceController(IDeviceGroupService deviceGroupService
            , IEventSourceService eventSourceService, EventSourceCollectService eventSourceCollectService) {
        this.deviceGroupService = deviceGroupService;
        this.eventSourceService = eventSourceService;
        this.eventSourceCollectService = eventSourceCollectService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览事件源功能")
    @CheckPermission({"iot:eventSource:view"})
    public Result<IPage<EventSource>> list(Page<EventSource> page, EventSource entity) {
        return this.eventSourceService.detailPage(page, entity);
    }

    /**
     * 事件源列表
     * @param entity
     * @return
     */
    @GetMapping("/list")
    public Result<List<EventSource>> list(EventSource entity) {
        return this.eventSourceService.list(entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:eventSource:edit"})
    public Result<EventSource> getById(Long id) {
        return this.eventSourceService.detailById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新事件源记录")
    @CheckPermission(value = {"iot:eventSource:edit", "iot:eventSource:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody EventSource entity) {
        return this.eventSourceService.saveOrUpdate(entity);
    }

    /**
     * 配置物模型接口
     * @param entity
     * @return
     */
    @PostMapping("/config")
    @Logger("事件源配置")
    @CheckPermission(value = {"iot:eventSource:config"})
    public Result<Boolean> config(@RequestBody EventSource entity) {
        this.eventSourceService.updateConfig(entity);
        return success();
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除事件源记录")
    @CheckPermission({"iot:eventSource:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.eventSourceService.removeAllByIds(idList);
    }

    /**
     * 事件源类型
     * @return
     */
    @GetMapping("types")
    public Result<List<IVOption>> types() {
        return success(EventSourceType.options());
    }

    /**
     * 获取设备组列表
     * @return
     */
    @GetMapping("deviceGroups")
    public Result<List<IVOption>> deviceGroups() {
        List<IVOption> options = deviceGroupService.list().stream()
                .map(deviceGroup -> new IVOption(deviceGroup.getName(), deviceGroup.getId()))
                .collect(Collectors.toList());
        return success(options);
    }

    /**
     * 切换事件源状态
     * @param status
     * @param id
     * @return
     */
    @Logger("切换事件源状态")
    @GetMapping("switch/{status}")
    public Result<Boolean> switchStatus(@PathVariable FuncStatus status, Long id) {
        return this.eventSourceCollectService.switchTask(id, status);
    }
}

