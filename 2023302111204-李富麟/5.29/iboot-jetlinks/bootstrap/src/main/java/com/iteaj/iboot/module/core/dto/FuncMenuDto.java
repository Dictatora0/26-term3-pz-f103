package com.iteaj.iboot.module.core.dto;

import lombok.Data;

@Data
public class FuncMenuDto {
    /**
     * 菜单id
     */
    private Long id;
    /**
     * 菜单名称
     */
    private String name;

    public FuncMenuDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
