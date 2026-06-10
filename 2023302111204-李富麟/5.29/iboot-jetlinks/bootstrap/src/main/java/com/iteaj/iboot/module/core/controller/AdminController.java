package com.iteaj.iboot.module.core.controller;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.CheckScope;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.excel.ExcelExportParams;
import com.iteaj.framework.utils.ExcelUtils;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.dto.PasswordDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.IAdminService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/core/admin")
public class AdminController extends BaseController {

    private final IAdminService adminService;

    public AdminController(IAdminService adminService) {
        this.adminService = adminService;
    }

    @Logger("admin_view")
    @GetMapping("/view")
    @CheckPermission("core:admin:view")
    @CheckScope("user.manage")
    public Result view(Page page, Admin admin) {
        return this.adminService.pageDetail(page, admin);
    }

    @Logger("admin_add")
    @PostMapping("/add")
    @CheckPermission("core:admin:add")
    @CheckScope("user.manage")
    public Result add(@RequestBody AdminDto admin) {
        this.adminService.createAdmin(admin);
        return success();
    }

    @Logger("admin_edit_get")
    @GetMapping("/edit")
    @CheckPermission("core:admin:edit")
    @CheckScope("user.manage")
    public Result<AdminDto> edit(Long id) {
        return this.adminService.getAdminDetailById(id);
    }

    @Logger("admin_edit")
    @PostMapping("/edit")
    @CheckPermission("core:admin:edit")
    @CheckScope("user.manage")
    public Result<Boolean> edit(@RequestBody AdminDto admin) {
        this.adminService.updateAdminAndRole(admin);
        return success(true);
    }

    @GetMapping("detail")
    public Result<AdminDto> detail() {
        Serializable id = SecurityUtil.getLoginId().orElse(null);
        return adminService.getAdminCenter(id);
    }

    @Logger("admin_delete")
    @PostMapping("/del")
    @CheckPermission("core:admin:del")
    @CheckScope("user.manage")
    public Result<Boolean> del(@RequestBody List<Long> list) {
        this.adminService.deleteAllJoinByIds(list);
        return success();
    }

    @Logger("admin_import")
    @PostMapping("import")
    @CheckPermission("core:admin:add")
    @CheckScope("user.manage")
    public Result<String> excelImport(MultipartFile file) throws IOException {
        List<Admin> admins = ExcelUtils.importExcel(file, Admin.class, new ImportParams());
        this.adminService.saveBatch(admins);
        return success("import success");
    }

    @Logger("admin_export")
    @GetMapping("export")
    @CheckPermission("core:admin:view")
    @CheckScope("user.manage")
    public void excelExport(Page<Admin> page, Admin admin, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        this.adminService.page(page, admin).ifPresent((a, b) -> {
            try {
                if (!CollectionUtils.isEmpty(a.getRecords())) {
                    ExcelUtils.exportExcel(a.getRecords(), Admin.class, new ExcelExportParams(), outputStream);
                } else {
                    throw new ServiceException("no data to export");
                }
            } catch (IOException e) {
                throw new ServiceException(e.getMessage(), e);
            }
        });
    }

    @Logger("admin_update_profile")
    @PostMapping("/modUserInfo")
    public Result modUserInfo(@RequestBody Admin admin) {
        return SecurityUtil.getLoginUser()
                .map(item -> {
                    admin.setId((Long) item.getId());
                    this.adminService.updateCurrentUserInfo(admin);
                    return success();
                }).orElse(fail("not logged in"));
    }

    @Logger("admin_reset_password")
    @PostMapping("pwd")
    @CheckPermission("core:admin:pwd")
    @CheckScope("user.manage")
    public Result updatePwd(@RequestBody PasswordDto passwordDto) {
        this.adminService.setAdminPassword(passwordDto.getId(), passwordDto.getPassword());
        return success("password updated");
    }

    @Logger("admin_update_current_password")
    @PostMapping("updateCurrentPwd")
    public Result updateCurrentPwd(@RequestBody PasswordDto passwordDto) {
        return SecurityUtil.getLoginId()
                .map(loginId -> {
                    this.adminService.updatePwdById(loginId, passwordDto.getPassword(), passwordDto.getOldPwd());
                    return success("password updated");
                }).orElse(fail("not logged in"));
    }
}
