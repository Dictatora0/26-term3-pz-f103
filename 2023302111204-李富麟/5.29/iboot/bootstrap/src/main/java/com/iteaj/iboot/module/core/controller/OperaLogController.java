package com.iteaj.iboot.module.core.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.core.entity.AccessLog;
import com.iteaj.iboot.module.core.service.IAccessLogService;
import com.iteaj.framework.security.CheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  日志管理功能
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/core/log")
public class OperaLogController extends BaseController {

    private final IAccessLogService operaLogService;

    public OperaLogController(IAccessLogService operaLogService) {
        this.operaLogService = operaLogService;
    }

    /**
     * 获取日志记录
     * @param page
     * @param accessLog
     * @return
     */
    @Logger("查看操作日志")
    @GetMapping("/view")
    @CheckPermission("core:log:view")
    public Result view(Page page, AccessLog accessLog) {
        return this.operaLogService.page(page, accessLog);
    }

    /**
     * 删除日志记录
     * @param list
     * @return
     */
    @Logger("删除操作日志")
    @PostMapping("/del")
    @CheckPermission("core:log:del")
    public Result del(@RequestBody List<Long> list) {
        return this.operaLogService.removeByIds(list);
    }
}
