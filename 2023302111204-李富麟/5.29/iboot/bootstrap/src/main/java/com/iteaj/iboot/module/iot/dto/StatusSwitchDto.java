package com.iteaj.iboot.module.iot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusSwitchDto<S> {

    /**
     * 操作id
     */
    private Long id;

    /**
     * 操作状态
     */
    private S status;
}
