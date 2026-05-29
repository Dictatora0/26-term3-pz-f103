package com.iteaj.framework.utils;

import com.iteaj.framework.TreeEntity;
import com.iteaj.framework.spi.admin.MenuResource;
import com.iteaj.framework.spi.admin.MenuType;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TreeUtils {

    public static <T extends TreeEntity> Collection<T> toTrees(Collection<T> list) {
        return toTrees(list, null);
    }

    public static <T extends TreeEntity> Collection<T> toTrees(Collection<T> list, Long parent) {

        if(CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        Map<String, T> menuMap = list.stream().collect(Collectors
                .toMap(item -> item.getId().toString(), item -> item));

        List<T> parents = new ArrayList<>();
        // 查找每条记录的父级, 并挂到父级的children里面
        list.forEach(item->{
            T parentItem = menuMap.get(item.getPid().toString());
            if(null != parentItem) {
                parentItem.addChild(item);
            } else {
                parents.add(item); // 没有找到父级, 说明自己已经是顶级
            }
        });

        if(parent != null) {
            return list.stream()
                    .filter(item -> item.getPid().equals(parent))
                    .collect(Collectors.toList());
        } else {
            return parents;
        }
    }

    public static <T extends TreeEntity> Collection<T> toAdminMenuTrees(List<T> allMenus, List<String> adminMenuIds) {
        Map<String, T> menuMap = allMenus.stream().collect(Collectors
                .toMap(item -> item.getId().toString(), item -> item));

        Map<String, T> adminMenus = new HashMap<>(); // 存放管理员菜单的列表
        adminMenuIds.forEach(item -> {
            T t = menuMap.get(item);
            if(t != null) {
                if(!adminMenus.containsKey(item)) {
                    adminMenus.put(item, t);
                }

                getParentMenu(t.getPid(), menuMap, adminMenus);
            }
        });

        // 过滤出非功能的菜单并且排序
        List<T> collect = adminMenus.values().stream().filter(item -> {
            if(item instanceof MenuResource) {
                return ((MenuResource) item).getType() != MenuType.A;
            }

            return true;
        }).sorted().collect(Collectors.toList());
        return toTrees(collect, 0l);
    }

    private static <T extends TreeEntity> void getParentMenu(Long pid, Map<String, T> menuMap, Map<String, T> adminMenus) {
        if(pid == null) return;
        String pidStr = String.valueOf(pid);
        T parentMenu = menuMap.get(pidStr);
        if(parentMenu != null && !adminMenus.containsKey(pidStr)) {
            parentMenu.setChildren(null);
            adminMenus.put(pidStr, parentMenu);
            getParentMenu(parentMenu.getPid(), menuMap, adminMenus);
        }
    }
}
