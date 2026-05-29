package com.iteaj.iboot.module.core.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.mapper.IAdminDao;
import com.iteaj.iboot.module.core.service.IAdminService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service
public class AdminServiceImpl extends BaseServiceImpl<IAdminDao, Admin> implements IAdminService, AuthenticationService {

    @Override
    public Admin getByAccount(String username) {
        return getBaseMapper().selectOne(new QueryWrapper<Admin>()
                .eq("account", username));
    }

    /**
     * 创建管理员
     * @param adminDto
     */
    @Override
    @Transactional
    public void createAdmin(AdminDto adminDto) {
        if(null == adminDto) throw new ServiceException("创建失败");
        Admin byAccount = this.getByAccount(adminDto.getAccount());
        if(null != byAccount) throw new ServiceException("此账号已经存在："+adminDto.getAccount());

        if(StrUtil.isNotBlank(adminDto.getPassword())) {
            adminDto.setPassword(SecureUtil.md5(adminDto.getPassword()));
        } else { // 没有设置密码则密码初始为账号
            adminDto.setPassword(SecureUtil.md5(adminDto.getAccount()));
        }

        this.getBaseMapper().createAdmin(adminDto);
    }

    @Transactional
    public void updateAdminAndRole(AdminDto adminDto) {
        this.updateById(adminDto.setUpdateTime(new Date()));
        this.getBaseMapper().updateAdminRole(adminDto);
    }

    @Override
    public DetailResult<AdminDto> getAdminDetailById(Long id) {
        return new DetailResult<>(getBaseMapper().getAdminDetailById(id));
    }

    /**
     * 更新用户密码
     * @param id 用户id
     * @param password 新密码
     * @param oldPwd 旧密码
     */
    @Override
    public void updatePwdById(Long id, String password, String oldPwd) {
        if(null == id || StrUtil.isBlank(password))
            throw new ServiceException("更新密码失败");
        if(StrUtil.isBlank(oldPwd))
            throw new ServiceException("请输入原密码");

        oldPwd = DigestUtils.md5Hex(oldPwd.getBytes());
        String oldPassword = getBaseMapper().getAdminPassword(id);

        if(!oldPwd.equalsIgnoreCase(oldPassword)) {
            throw new ServiceException("密码不匹配");
        }

        String md5Hex = DigestUtils.md5Hex(password.getBytes());
        getBaseMapper().updatePwdById(id, md5Hex);
    }

    @Override
    public void deleteAllJoinByIds(List<Long> list) {
        if(CollectionUtil.isNotEmpty(list)) {
            getBaseMapper().deleteAllJoinByIds(list);
        }
    }

    @Override
    public List<String> selectPermsById(Serializable id) {
        return getBaseMapper().selectPermsById(id);
    }

    @Override
    public void updateCurrentUserInfo(Admin admin) {
        this.updateById(admin);

        // 更新当前登录的信息
        Admin principal = (Admin) SecurityUtil.getLoginUser().get();
        principal.setAvatar(admin.getAvatar()).setEmail(admin.getEmail())
                .setPhone(admin.getPhone()).setRemark(admin.getRemark())
                .setName(admin.getName()).setSex(admin.getSex());
    }

    @Override
    public DetailResult<AdminDto> getAdminCenter(Serializable id) {
        return new DetailResult<>(getBaseMapper().getAdminCenter(id));
    }

    @Override
    public void setAdminPassword(Long id, String password) {
        if(null == id || StrUtil.isBlank(password))
            throw new ServiceException("请检查要设置的密码");

        this.getById(id).ofNullable().orElseThrow(()
                -> new ServiceException("用户不存在"));

        String md5Hex = DigestUtils.md5Hex(password.getBytes());
        getBaseMapper().updatePwdById(id, md5Hex);
    }

    @Override
    public PageResult<IPage<AdminDto>> pageDetail(Page page, Admin admin) {
        return new PageResult<>(getBaseMapper().pageDetail(page, admin));
    }

}
