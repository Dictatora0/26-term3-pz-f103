package com.iteaj.iboot.module.core.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.admin.event.OnlineStatus;
import com.iteaj.iboot.module.core.dto.OnlineCountDto;
import com.iteaj.iboot.module.core.entity.OnlineUser;
import com.iteaj.iboot.module.core.service.IOnlineUserService;
import com.iteaj.framework.security.CheckPermission;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 在线用户管理
 * @author iteaj
 */
@RestController
@RequestMapping("/core/online")
public class OnlineUserController extends BaseController {

    private final IOnlineUserService onlineUserService;

    public OnlineUserController(IOnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity
    */
    @Logger("查询在线用户")
    @GetMapping("/view")
    @CheckPermission(value = {"core:online:view"})
    public Result<IPage<OnlineUser>> list(Page<OnlineUser> page, OnlineUser entity) {
        page.addOrder(OrderItem.asc("status"), OrderItem.desc("login_time"));
        return this.onlineUserService.page(page, entity);
    }

    /**
    * 删除在线记录
    * @param idList
    */
    @Logger("删除在线用户记录")
    @PostMapping("/del")
    @CheckPermission(value = {"core:online:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        if(idList.size() == 1) {
            this.onlineUserService.getById(idList.get(0)).ofNullable().ifPresent(item -> {
//                if(item.getStatus() == OnlineStatus.Online
//                        && null == ShiroUtil.getSession(item.getSessionId())) {
//                    throw new IllegalStateException("客户端在线, 请先剔除");
//                }
            });
        } else {
            this.onlineUserService.listByIds(idList).forEach(item -> {
                if(item.getStatus() == OnlineStatus.Online) {
                    idList.remove(item.getId());
                }
            });
        }

        return this.onlineUserService.removeByIds(idList);
    }

    /**
     * 剔除用户下线
     * @param entity
     * @return
     */
    @Logger("剔除用户下线")
    @PostMapping("offline")
    @CheckPermission(value = {"core:online:offline"})
    public Result<Boolean> offline(@RequestBody OnlineUser entity) {
        if(entity.getStatus() == OnlineStatus.Offline) {
            return fail("此用户不在线");
        }

        if(SecurityUtil.isCurrentUser(entity.getSessionId())) {
            return fail("不允许剔除自己");
        }

        SecurityUtil.logout(entity.getSessionId());
        return success("剔除成功");
    }

    /**
     * 统计当天用户信息
     * @return
     */
    @GetMapping("countToday")
    public Result<OnlineCountDto> countToday() {
        OnlineCountDto countDto = onlineUserService.countCurrentOnline();
        return success(countDto);
    }

    /**
     * 统计最近一个月的访问用户
     * @return
     */
    @GetMapping("countLastMonth")
    public Result<List> countLastMonth() {

        return success();
    }
}

