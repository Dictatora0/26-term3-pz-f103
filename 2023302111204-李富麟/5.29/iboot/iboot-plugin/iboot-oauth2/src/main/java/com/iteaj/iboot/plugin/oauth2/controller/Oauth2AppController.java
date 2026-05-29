package com.iteaj.iboot.plugin.oauth2.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2App;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 子应用管理
 */
@RestController
@RequestMapping("/oauth2/app")
public class Oauth2AppController {

    private final Oauth2AppService oauth2AppService;

    public Oauth2AppController(Oauth2AppService oauth2AppService) {
        this.oauth2AppService = oauth2AppService;
    }

    /**
     * 浏览应用功能
     * @param page
     * @param app
     * @return
     */
    @Logger("浏览子应用功能")
    @GetMapping("view")
    @CheckPermission(value = {"oauth2:app:view"})
    public Result<IPage<Oauth2App>> view(Page page, Oauth2App app) {
        page.addOrder(OrderItem.desc("createTime"));
        return oauth2AppService.page(page, app);
    }

    /**
     * 获取所有应用
     * @return
     */
    @GetMapping("list")
    public Result<List<Oauth2App>> list() {
        return oauth2AppService.list(Wrappers.<Oauth2App>lambdaQuery()
                .orderByDesc(Oauth2App::getCreateTime));
    }

    /**
     * 获取应用详情
     * @param id 应用id
     * @return
     */
    @GetMapping("edit")
    @CheckPermission(value = {"oauth2:app:edit"})
    public DetailResult<Oauth2App> edit(Long id) {
        return oauth2AppService.getById(id);
    }

    /**
     * 新增或者更新记录
     * @param app
     * @return
     */
    @Logger("新增或者更新记录")
    @PostMapping("saveOrUpdate")
    @CheckPermission(value = {"oauth2:app:edit", "oauth2:app:add"}, logical = Logical.OR)
    public BooleanResult saveOrUpdate(@RequestBody Oauth2App app) {
        return oauth2AppService.saveOrUpdate(app);
    }

    /**
     * 删除应用记录
     * @param list
     * @return
     */
    @Logger("删除应用记录")
    @PostMapping("/del")
    @CheckPermission("core:config:del")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        return this.oauth2AppService.removeByIds(list);
    }
}
