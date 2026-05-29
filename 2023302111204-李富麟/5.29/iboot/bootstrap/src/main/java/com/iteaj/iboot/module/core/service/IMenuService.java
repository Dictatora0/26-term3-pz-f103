package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.ListResult;
import com.iteaj.iboot.module.core.entity.Menu;

import java.util.List;

public interface IMenuService extends IBaseService<Menu> {

    /**
     * 查询菜单树
     * 默认不对状态status作为查询条件
     * @param menu
     * @return
     */
    ListResult<Menu> selectMenuTrees(Menu menu);

    /**
     * 查询前端菜单栏所使用的菜单树
     * @param aid 管理员id
     * @param isSuper 是否是超级管理员
     * @return
     */
    ListResult<Menu> selectMenuBarTrees(Long aid,  boolean isSuper);

    /**
     * 不包含菜单类型是权限的菜单
     * @return
     */
    ListResult<Menu> selectParentTrees();

    /**
     * 通过url地址获取菜单
     * @param url
     * @return
     */
    Menu getByUrl(String url);

    /**
     * 获取自定管理员拥有的权限
     * @param adminId
     * @return
     */
    List<String> selectPermissions(Long adminId);
}
