package com.iteaj.iboot.module.iot.debug.tcp.server;

import com.iteaj.iot.message.DefaultMessageHead;
import com.iteaj.iot.server.ServerMessage;

public class DebugServerMessage extends ServerMessage {

    public DebugServerMessage(byte[] message) {
        super(message);
    }

    @Override
    protected MessageHead doBuild(byte[] message) {
        return new DefaultMessageHead(getChannelId(), null, DebugProtocolType.SERVER_DEBUG);
    }
}
