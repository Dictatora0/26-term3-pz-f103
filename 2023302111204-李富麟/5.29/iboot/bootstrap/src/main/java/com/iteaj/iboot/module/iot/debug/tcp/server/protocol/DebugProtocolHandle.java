package com.iteaj.iboot.module.iot.debug.tcp.server.protocol;

import com.iteaj.iot.server.ServerProtocolHandle;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DebugProtocolHandle implements ServerProtocolHandle<DebugProtocol> {

    @Override
    public Object handle(DebugProtocol protocol) {
        protocol.response("你好啊, 我已经收到了".getBytes(StandardCharsets.UTF_8));
        return null;
    }
}
