package com.iteaj.iboot.module.iot.debug.tcp.server.protocol;

import com.iteaj.iboot.module.iot.debug.tcp.server.DebugProtocolType;
import com.iteaj.iboot.module.iot.debug.tcp.server.DebugServerMessage;
import com.iteaj.iot.ProtocolType;
import com.iteaj.iot.server.protocol.ClientInitiativeProtocol;

public class DebugProtocol extends ClientInitiativeProtocol<DebugServerMessage> {

    private DebugServerMessage responseMessage;

    public DebugProtocol(DebugServerMessage requestMessage) {
        super(requestMessage);
    }

    public void response(byte[] message) {
        this.responseMessage = new DebugServerMessage(message);
    }

    @Override
    protected DebugServerMessage doBuildResponseMessage() {
        return responseMessage;
    }

    @Override
    protected void doBuildRequestMessage(DebugServerMessage requestMessage) { }

    @Override
    public ProtocolType protocolType() {
        return DebugProtocolType.SERVER_DEBUG;
    }
}
