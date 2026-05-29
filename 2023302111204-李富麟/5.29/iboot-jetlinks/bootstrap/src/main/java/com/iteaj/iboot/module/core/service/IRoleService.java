package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.iboot.module.core.dto.RoleDto;
import com.iteaj.iboot.module.core.entity.Role;

import java.io.Serializable;
import java.util.List;

/**
 * create time: 2019/11/27
 *
 * @author iteaj
 * @since 1.0
 */
public interface IRoleService extends IBaseService<Role> {

    void delRoleAndPermByIds(List<Long> list);

    DetailResult<RoleDto> detail(Long id);

    void createRoleAndPerms(RoleDto role);

    void updateRolePermsById(RoleDto role);

    List<Long> selectByAdminId(Serializable id);

    /**
     * 获取指定角色对应的权限列表
     * @param roleId 角色id
     * @return
     */
    ListResult<Long> listMenusOfRole(Long roleId);

    /**
     * 获取指定角色绑定的管理员
     * @param roleId
     * @return
     */
    List<Long> listBindAdminOfRole(Long roleId);
}
