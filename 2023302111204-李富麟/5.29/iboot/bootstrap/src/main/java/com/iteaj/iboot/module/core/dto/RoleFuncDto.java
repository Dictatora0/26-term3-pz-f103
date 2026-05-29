package com.iteaj.iboot.module.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RoleFuncDto {

    private Long id;

    private List<Long> menuIds;

    public RoleFuncDto(Long id, List<Long> menuIds) {
        this.id = id;
        this.menuIds = menuIds;
    }
}
