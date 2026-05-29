package com.iteaj.iboot.module.core.dto;

import com.iteaj.iboot.module.core.entity.Role;

import java.util.List;

/**
 * create time: 2019/11/27
 *
 * @author iteaj
 * @since 1.0
 */
public class RoleDto extends Role {

    private List<Long> menuIds;

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public RoleDto setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
        return this;
    }
}
