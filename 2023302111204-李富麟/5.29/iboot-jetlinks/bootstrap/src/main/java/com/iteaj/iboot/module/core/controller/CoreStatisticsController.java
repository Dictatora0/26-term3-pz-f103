package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.core.dto.OnlineCountDto;
import com.iteaj.iboot.module.core.service.IOnlineUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统功能统计
 */
@RestController
@RequestMapping("/core/statistics")
public class CoreStatisticsController extends BaseController {

    private final IOnlineUserService onlineUserService;

    public CoreStatisticsController(IOnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    /**
     * 当天在线人数和在前在线人数
     * @return
     */
    @GetMapping("/online")
    public Result<OnlineCountDto> todayOnline() {
        return success(onlineUserService.countCurrentOnline());
    }

    /**
     * 近一个月在线用户统计
     * @return
     */
    @GetMapping("/inRecentMonth")
    public Result online() {
        return success();
    }
}
