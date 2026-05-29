package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.core.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMenuDao extends BaseMapper<Menu> {

    /**
     * 查找指定管理员所拥有的菜单权限
     * @param aid
     * @return
     */
    List<String> selectAdminMenus(Long aid);

    /**
     * 查询管理员的菜单权限
     * @param adminId
     * @return
     */
    List<String> selectPermissions(Long adminId);
}
