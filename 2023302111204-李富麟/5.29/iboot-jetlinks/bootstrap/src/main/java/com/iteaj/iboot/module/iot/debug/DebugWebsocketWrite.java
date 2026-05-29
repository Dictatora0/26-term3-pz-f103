package com.iteaj.iboot.module.iot.debug;

import com.alibaba.fastjson.JSON;
import com.iteaj.framework.result.HttpResult;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerProtocol;

public class DebugWebsocketWrite {

    private String clientSn;

    public DebugWebsocketWrite(String clientSn) {
        this.clientSn = clientSn;
    }

    public void write(HttpResult result) {
        DefaultWebSocketServerProtocol.write(this.clientSn, JSON.toJSONString(result));
    }

    public String getClientSn() {
        return clientSn;
    }
}
