package com.iteaj.iboot.module.iot.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.iot.entity.EventSourceDetail;
import com.iteaj.iboot.module.iot.service.IEventSourceDetailService;
import com.iteaj.framework.BaseController;

/**
 * 事件源详情管理
 * @author iteaj
 * @since 2024-02-27
 */
@RestController
@RequestMapping("/iot/eventSourceDetail")
public class EventSourceDetailController extends BaseController {

    private final IEventSourceDetailService eventSourceDetailService;

    public EventSourceDetailController(IEventSourceDetailService eventSourceDetailService) {
        this.eventSourceDetailService = eventSourceDetailService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览事件源详情功能")
    @CheckPermission({"iot:eventSourceDetail:view"})
    public Result<IPage<EventSourceDetail>> list(Page<EventSourceDetail> page, EventSourceDetail entity) {
        return this.eventSourceDetailService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:eventSourceDetail:edit"})
    public Result<EventSourceDetail> getById(Long id) {
        return this.eventSourceDetailService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新事件源详情记录")
    @CheckPermission(value = {"iot:eventSourceDetail:edit", "iot:eventSourceDetail:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody EventSourceDetail entity) {
        return this.eventSourceDetailService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除事件源详情记录")
    @CheckPermission({"iot:eventSourceDetail:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.eventSourceDetailService.removeByIds(idList);
    }
}

