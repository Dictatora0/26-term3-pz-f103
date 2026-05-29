package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.core.dto.RoleDto;
import com.iteaj.iboot.module.core.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.List;

/**
 * create time: 2019/11/27
 *
 * @author iteaj
 * @since 1.0
 */
@Mapper
public interface IRoleDao extends BaseMapper<Role> {

    RoleDto joinRoleMenuById(Long id);

    void updateRolePermsById(Role role);

    void createRoleAndPerms(RoleDto role);

    void deleteAllJoinByIds(List<Long> list);

    List<Long> selectByAdminId(Serializable id);

    /**
     * 获取指定角色下面的菜单列表(权限)
     * @param roleId
     * @return
     */
    List<Long> listMenusOfRole(Long roleId);

    /**
     * 获取指定角色绑定的管理员
     * @param roleId
     * @return
     */
    List<Long> listBindAdminOfRole(Long roleId);
}
