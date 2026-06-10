package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.iboot.module.core.dto.PasswordDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.IAdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户中心
 */
@RestController
@RequestMapping("/core/center")
public class AdminCenterController extends BaseController {

    private final IAdminService adminService;

    public AdminCenterController(IAdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 只允许修改当前登录用户自己的资料
     */
    @Logger("修改用户中心资料")
    @PostMapping("editUser")
    public Result<Boolean> updateUser(@RequestBody Admin admin) {
        return SecurityUtil.getLoginId()
                .map(loginId -> {
                    admin.setId(loginId);
                    adminService.updateCurrentUserInfo(admin);
                    return success(true);
                }).orElse(fail("not logged in"));
    }

    /**
     * 只允许修改当前登录用户自己的密码
     */
    @Logger("修改用户中心密码")
    @PostMapping("pwd")
    public Result<Boolean> updatePwd(@RequestBody PasswordDto passwordDto) {
        return SecurityUtil.getLoginId()
                .map(loginId -> {
                    this.adminService.updatePwdById(loginId, passwordDto.getPassword(), passwordDto.getOldPwd());
                    return success(true);
                }).orElse(fail("not logged in"));
    }
}
