package com.iteaj.iboot.module.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.entity.Admin;

import java.io.Serializable;
import java.util.List;

public interface IAdminService extends IBaseService<Admin> {

    /**
     * 通过用户名获取账号
     * @param username
     * @return
     */
    Admin getByAccount(String username);

    void createAdmin(AdminDto adminDto);

    /**
     * 更新管理员
     * @param adminDto
     */
    void updateAdminAndRole(AdminDto adminDto);

    DetailResult<AdminDto> getAdminDetailById(Long id);

    /**
     * 更新密码
     */
    void updatePwdById(Long id, String password, String oldPwd);

    void deleteAllJoinByIds(List<Long> list);

    List<String> selectPermsById(Serializable id);

    void updateCurrentUserInfo(Admin admin);

    /**
     * 获取管理中心的用户详情
     * @param id
     * @return
     */
    DetailResult<AdminDto> getAdminCenter(Serializable id);

    /**
     * 设置密码
     * @param id
     * @param password
     */
    void setAdminPassword(Long id, String password);

    /**
     * 查询用户详情
     * @param page
     * @param admin
     * @return
     */
    PageResult<IPage<AdminDto>> pageDetail(Page page, Admin admin);
}
