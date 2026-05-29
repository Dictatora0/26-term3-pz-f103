package com.iteaj.iboot.module.core.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.core.entity.Notify;
import com.iteaj.iboot.module.core.service.INotifyService;
import com.iteaj.framework.BaseController;

/**
 * 系统通知管理
 * @author iteaj
 * @since 2023-08-17
 */
@RestController
@RequestMapping("/core/notify")
public class NotifyController extends BaseController {

    private final INotifyService notifyService;

    public NotifyController(INotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览系统通知功能")
    @CheckPermission({"core:notify:view"})
    public Result<IPage<Notify>> list(Page<Notify> page, Notify entity) {
        return this.notifyService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"core:notify:edit"})
    public Result<Notify> getById(Long id) {
        return this.notifyService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新系统通知记录")
    @CheckPermission(value = {"core:notify:edit", "core:notify:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody Notify entity) {
        return this.notifyService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除系统通知记录")
    @CheckPermission({"core:notify:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.notifyService.removeByIds(idList);
    }
}

