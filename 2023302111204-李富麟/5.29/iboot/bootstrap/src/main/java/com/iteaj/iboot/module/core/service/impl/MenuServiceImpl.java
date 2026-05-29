package com.iteaj.iboot.module.core.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.consts.PermStatus;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.admin.MenuType;
import com.iteaj.framework.spi.admin.Module;
import com.iteaj.framework.utils.TreeUtils;
import com.iteaj.iboot.module.core.entity.Menu;
import com.iteaj.iboot.module.core.mapper.IMenuDao;
import com.iteaj.iboot.module.core.service.IMenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends BaseServiceImpl<IMenuDao, Menu> implements IMenuService {

    private final List<Module> modules;
    public MenuServiceImpl(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public BooleanResult save(Menu entity) {
        getOne(Wrappers.<Menu>lambdaQuery()
                .eq(Menu::getName, entity.getName())
                .eq(Menu::getPid, entity.getPid()))
                .ofNullable()
                .ifPresent(menu -> {throw new ServiceException("同级已经存在菜单名称["+entity.getName()+"]");});

        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(Menu entity) {
        getOne(Wrappers.<Menu>lambdaQuery()
                .ne(Menu::getId, entity.getId())
                .eq(Menu::getPid, entity.getPid())
                .eq(Menu::getName, entity.getName()))
                .ofNullable().ifPresent(menu -> {
                    throw new ServiceException("同级已经存在菜单名称["+entity.getName()+"]");
                });

        entity.setUpdateTime(new Date());
        return super.updateById(entity);
    }

    @Override
    public ListResult<Menu> selectMenuTrees(Menu menu) {
        List<Menu> menuList = this.getBaseMapper() // 根据sort排序后的数据
                .selectList(Wrappers.query(menu).orderByAsc("sort"));

        List<Menu> menus = (List<Menu>) TreeUtils.toTrees(menuList, menu.getPid());
        return new ListResult<>(menus);
    }

    @Override
    public ListResult<Menu> selectMenuBarTrees(Long aid, boolean isSuper) {
        // 只查询已经注册过的模块的菜单列表
        List<String> msn = modules.stream()
                .map(item -> item.getMsn())
                .collect(Collectors.toList());

        if(isSuper) { // 超级管理员拥有所有菜单
            List<Menu> allMenus = this.getBaseMapper()
                    .selectList(Wrappers.<Menu>lambdaQuery()
                            .ne(Menu::getType, MenuType.A)
                            .ne(Menu::getStatus, PermStatus.disabled)
                            .in(Menu::getMsn, msn)
                            .orderByAsc(Menu::getSort));

            if(CollectionUtils.isEmpty(allMenus)) {
                return new ListResult<>(Collections.emptyList());
            }

            // 转成树结构
            List<Menu> menus = (List<Menu>) TreeUtils.toTrees(allMenus, 0l);
            return new ListResult<>(menus);
        } else { // 普通管理员的菜单列表
            List<Menu> allMenus = this.getBaseMapper()
                    .selectList(Wrappers.<Menu>lambdaQuery()
                            .ne(Menu::getStatus, PermStatus.disabled)
                            .in(Menu::getMsn, msn));

            if(CollectionUtils.isEmpty(allMenus)) {
                return new ListResult<>(Collections.emptyList());
            }

            List<String> adminMenuIds = this.getBaseMapper().selectAdminMenus(aid);
            final Collection<Menu> menus = TreeUtils.toAdminMenuTrees(allMenus, adminMenuIds);
            return new ListResult<>(menus.stream().collect(Collectors.toList()));
        }
    }

    @Override
    public ListResult selectParentTrees() {
        // 去除禁用的状态
        List<Menu> status = this.getBaseMapper()
                .selectList(Wrappers.<Menu>lambdaQuery()
                        .ne(Menu::getType, MenuType.A)
                        .ne(Menu::getStatus, PermStatus.disabled)
                        .orderByAsc(Menu::getSort));

        // 转成树结构
        List<Menu> menus = (List<Menu>) TreeUtils.toTrees(status, 0l);
        return new ListResult(menus);
    }

    @Override
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(CollectionUtils.isEmpty(idList)) {
            throw new ServiceException("未指定要删除的记录");
        }
        if(idList.size() > 1) {
            throw new ServiceException("菜单不支持批量删除");
        }

        idList.stream().forEach(item -> {
            this.count(Wrappers.<Menu>query().eq("pid", item))
                .ofNullable().filter(count -> count > 0)
                .ifPresent((menu) -> {
                    throw new ServiceException("请先删除子菜单");
                });
        });

        return super.removeByIds(idList);
    }

    @Override
    public Menu getByUrl(String url) {
        return getBaseMapper().selectOne(Wrappers.<Menu>query().eq("url", url));
    }

    @Override
    public List<String> selectPermissions(Long adminId) {
        List<String> permissions = new ArrayList<>();
        if(SecurityUtil.isSuper(adminId)) {
            this.getBaseMapper()
                    .selectList(Wrappers.<Menu>lambdaQuery()
                            .ne(Menu::getStatus, PermStatus.disabled)
                            .ne(Menu::getType, MenuType.M)
                            .ne(Menu::getType, MenuType.G)).stream()
                    .filter(item -> StringUtils.hasText(item.getPerms()))
                    .map(item -> item.getPerms().split(","))
                    .forEach(item -> permissions.addAll(Arrays.asList(item)));
        } else {
            List<String> selectPermissions = this.getBaseMapper().selectPermissions(adminId);
            if(!CollectionUtils.isEmpty(selectPermissions)) {
                selectPermissions.forEach(item -> {
                    if(StrUtil.isNotBlank(item)) {
                        permissions.addAll(Arrays.asList(item.split(",")));
                    }
                });
            }
        }
        return permissions;

    }

}
