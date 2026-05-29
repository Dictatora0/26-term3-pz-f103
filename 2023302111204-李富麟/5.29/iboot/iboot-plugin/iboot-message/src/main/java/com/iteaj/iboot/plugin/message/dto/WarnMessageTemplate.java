package com.iteaj.iboot.plugin.message.dto;

import com.iteaj.iboot.plugin.message.entity.MessageTemplate;
import lombok.Data;

@Data
public class WarnMessageTemplate extends MessageTemplate {

    /**
     * 设备名称
     */
    private String deviceName;
}
