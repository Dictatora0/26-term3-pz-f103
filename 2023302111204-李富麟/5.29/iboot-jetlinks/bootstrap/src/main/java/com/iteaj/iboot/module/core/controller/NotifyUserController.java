package com.iteaj.iboot.module.core.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.core.entity.NotifyUser;
import com.iteaj.iboot.module.core.service.INotifyUserService;
import com.iteaj.framework.BaseController;

/**
 * 消息通知用户管理
 * @author iteaj
 * @since 2023-09-08
 */
@RestController
@RequestMapping("/core/notifyUser")
public class NotifyUserController extends BaseController {

    private final INotifyUserService notifyUserService;

    public NotifyUserController(INotifyUserService notifyUserService) {
        this.notifyUserService = notifyUserService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览消息通知用户功能")
    @CheckPermission({"core:notifyUser:view"})
    public Result<IPage<NotifyUser>> list(Page<NotifyUser> page, NotifyUser entity) {
        return this.notifyUserService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"core:notifyUser:edit"})
    public Result<NotifyUser> getById(Long id) {
        return this.notifyUserService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新消息通知用户记录")
    @CheckPermission(value = {"core:notifyUser:edit", "core:notifyUser:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody NotifyUser entity) {
        return this.notifyUserService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除消息通知用户记录")
    @CheckPermission({"core:notifyUser:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.notifyUserService.removeByIds(idList);
    }
}

