package com.iteaj.iboot.module.iot.debug.tcp.server;

import com.iteaj.iot.ProtocolType;

public enum DebugProtocolType implements ProtocolType {

    SERVER_DEBUG("服务端调试")
    ;
    private String desc;

    DebugProtocolType(String desc) {
        this.desc = desc;
    }

    @Override
    public Enum getType() {
        return this;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
