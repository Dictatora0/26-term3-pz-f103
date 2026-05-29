package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.dto.PasswordDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.IAdminService;
import com.iteaj.iboot.module.core.entity.Menu;
import com.iteaj.iboot.module.core.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * 后台用户管理中心
 */
@RestController
@RequestMapping("/core/center")
public class AdminCenterController extends BaseController {

    @Autowired
    private IMenuService menuService;
    @Autowired
    private IAdminService adminService;

    /**
     * 修改用户
     * @param admin
     * @return
     */
    @Logger("需改用户中心用户")
    @PostMapping("editUser")
    public Result<Boolean> updateUser(@RequestBody Admin admin) {
        return adminService.updateById(admin);
    }

    /**
     * 修改用户密码
     * @return
     */
    @Logger("修改用户中心用户密码")
    @PostMapping("pwd")
    public Result<Boolean> updatePwd(@RequestBody PasswordDto passwordDto) {
        this.adminService.updatePwdById(passwordDto.getId()
                , passwordDto.getPassword(), passwordDto.getOldPwd());
        return success("修改成功");
    }
}
