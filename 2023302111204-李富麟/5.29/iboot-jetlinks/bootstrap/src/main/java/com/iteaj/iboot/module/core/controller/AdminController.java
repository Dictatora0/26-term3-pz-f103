package com.iteaj.iboot.module.core.controller;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.excel.ExcelExportParams;
import com.iteaj.framework.utils.ExcelUtils;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.dto.PasswordDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.IAdminService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * 用户管理
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/core/admin")
public class AdminController extends BaseController {

    private final IAdminService adminService;

    public AdminController(IAdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 查询用户管理列表
     * @param page
     * @param admin
     * @return
     */
    @Logger("用户分页列表")
    @GetMapping("/view")
    @CheckPermission("core:admin:view")
    public Result view(Page page, Admin admin) {
        return this.adminService.pageDetail(page, admin);
    }

    /**
     * 新增用户及角色
     * @param admin
     * @return
     */
    @Logger("新增用户")
    @PostMapping("/add")
    @CheckPermission("core:admin:add")
    public Result add(@RequestBody AdminDto admin) {
        this.adminService.createAdmin(admin);
        return success();
    }

    /**
     * 获取用户详情
     * @param id
     * @return
     */
    @Logger("获取用户详情")
    @GetMapping("/edit")
    @CheckPermission("core:admin:edit")
    public Result<AdminDto> edit(Long id) {
        return this.adminService.getAdminDetailById(id);
    }

    /**
     * 修改用户及角色
     * @param admin
     * @return
     */
    @Logger("修改用户及角色")
    @PostMapping("/edit")
    @CheckPermission("core:admin:edit")
    public Result<Boolean> edit(@RequestBody AdminDto admin) {
        this.adminService.updateAdminAndRole(admin);
        return success(true);
    }

    /**
     * 获取用户详情
     * @return
     */
    @GetMapping("detail")
    public Result<AdminDto> detail() {
        Serializable id = SecurityUtil
                .getLoginId()
                .orElse(null);

        return adminService.getAdminCenter(id);
    }

    /**
     * 删除用户
     * @param list
     * @return
     */
    @Logger("删除用户")
    @PostMapping("/del")
    @CheckPermission("core:admin:del")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        this.adminService.deleteAllJoinByIds(list);
        return success();
    }

    /**
     * 导入用户
     * @param file
     */
    @Logger("导入用户")
    @PostMapping("import")
    public Result<String> excelImport(MultipartFile file) throws IOException {
        List<Admin> admins = ExcelUtils.importExcel(file, Admin.class, new ImportParams());
        this.adminService.saveBatch(admins);

        return success("导入成功");
    }

    /**
     * 导出用户
     * @param page
     * @param admin
     */
    @Logger("导出用户")
    @GetMapping("export")
    public void excelExport(Page<Admin> page, Admin admin, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        this.adminService.page(page, admin).ifPresent((a, b) -> {
            try {
                if(!CollectionUtils.isEmpty(a.getRecords())) {
                    ExcelUtils.exportExcel(a.getRecords(), Admin.class, new ExcelExportParams(), outputStream);
                } else {
                    throw new ServiceException("未指定要导出的数据");
                }
            } catch (IOException e) {
                throw new ServiceException(e.getMessage(), e);
            }
        });
    }

    /**
     * 修改用户信息
     * @param admin
     * @return
     */
    @Logger("修改用户")
    @PostMapping("/modUserInfo")
    public Result modUserInfo(@RequestBody Admin admin) {
        return SecurityUtil.getLoginUser()
                .map(item -> {
                    admin.setId((Long) item.getId());
                    this.adminService.updateCurrentUserInfo(admin);
                    return success();
                }).orElse(fail("未登录"));
    }

    /**
     * 设置用户密码
     * @return
     */
    @Logger("修改用户密码")
    @PostMapping("pwd")
    @CheckPermission("core:admin:pwd")
    public Result updatePwd(@RequestBody PasswordDto passwordDto) {
        this.adminService.setAdminPassword(passwordDto
                .getId(), passwordDto.getPassword());
        return success("设置成功");
    }

    /**
     * 设置用户密码
     * @return
     */
    @Logger("修改当前用户密码")
    @PostMapping("updateCurrentPwd")
    public Result updateCurrentPwd(@RequestBody PasswordDto passwordDto) {
        this.adminService.updatePwdById(passwordDto.getId()
                , passwordDto.getPassword(), passwordDto.getOldPwd());
        return success("设置成功");
    }
}
