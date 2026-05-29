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
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2User;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Oauth2用户管理
 */
@RestController
@RequestMapping("/oauth2/user")
public class Oauth2UserController {

    private final Oauth2UserService oauth2UserService;

    public Oauth2UserController(Oauth2UserService oauth2UserService) {
        this.oauth2UserService = oauth2UserService;
    }

    /**
     * 浏览用户功能
     * @param page
     * @param user
     * @return
     */
    @Logger("浏览子应用功能")
    @GetMapping("view")
    @CheckPermission(value = {"oauth2:user:view"})
    public Result<IPage<Oauth2App>> view(Page page, Oauth2User user) {
        page.addOrder(OrderItem.desc("createTime"));
        return oauth2UserService.page(page, user);
    }

    /**
     * 获取所有用户
     * @return
     */
    @GetMapping("list")
    public Result<List<Oauth2User>> list() {
        return oauth2UserService.list(Wrappers.<Oauth2User>lambdaQuery()
                .orderByDesc(Oauth2User::getCreateTime));
    }

    /**
     * 获取用户详情
     * @param id 应用id
     * @return
     */
    @GetMapping("edit")
    @CheckPermission(value = {"oauth2:user:edit"})
    public DetailResult<Oauth2User> edit(Long id) {
        return oauth2UserService.getById(id);
    }

    /**
     * 新增或者更新记录
     * @param app
     * @return
     */
    @Logger("新增或者更新记录")
    @PostMapping("saveOrUpdate")
    @CheckPermission(value = {"oauth2:user:edit", "oauth2:user:add"}, logical = Logical.OR)
    public BooleanResult saveOrUpdate(@RequestBody Oauth2User app) {
        return oauth2UserService.saveOrUpdate(app);
    }

    /**
     * 删除用户记录
     * @param list
     * @return
     */
    @Logger("删除应用记录")
    @PostMapping("/del")
    @CheckPermission("core:config:del")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        return this.oauth2UserService.removeByIds(list);
    }
}
