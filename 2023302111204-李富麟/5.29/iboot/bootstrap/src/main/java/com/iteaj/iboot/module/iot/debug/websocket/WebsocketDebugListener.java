package com.iteaj.iboot.module.iot.debug.websocket;

import com.alibaba.fastjson.JSON;
import com.iteaj.framework.result.HttpResult;
import com.iteaj.iboot.module.iot.debug.DebugHandle;
import com.iteaj.iboot.module.iot.debug.DebugHandleFactory;
import com.iteaj.iboot.module.iot.debug.DebugRequestModel;
import com.iteaj.iboot.module.iot.debug.DebugWebsocketWrite;
import com.iteaj.iot.CoreConst;
import com.iteaj.iot.Message;
import com.iteaj.iot.codec.filter.RegisterParams;
import com.iteaj.iot.server.websocket.WebSocketServerListener;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerComponent;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerProtocol;
import com.iteaj.iot.websocket.HttpRequestWrapper;
import com.iteaj.iot.websocket.WebSocketInterceptor;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Optional;

public class WebsocketDebugListener implements WebSocketServerListener, WebSocketInterceptor<DefaultWebSocketServerComponent> {

    private final DebugHandleFactory handleFactory;
    private final ThreadPoolTaskScheduler iotTaskScheduler;

    public WebsocketDebugListener(DebugHandleFactory handleFactory, ThreadPoolTaskScheduler iotTaskScheduler) {
        this.handleFactory = handleFactory;
        this.iotTaskScheduler = iotTaskScheduler;
    }

    @Override
    public String uri() {
        return "/ws/iot/debug";
    }

    @Override
    public void onText(DefaultWebSocketServerProtocol protocol) {
        HttpRequestWrapper request = protocol.requestMessage().request();
        Optional<String> type = request.getQueryParam("type");
        if(type.isPresent()) {
            Optional<DebugHandle> handle = this.handleFactory.getHandle(type.get());
            if(handle.isPresent()) {
                DebugHandle debugHandle = handle.get();
                DebugRequestModel parse = debugHandle.parse(protocol.readText());
                iotTaskScheduler.execute(() -> debugHandle.handle(parse.getModel(), new DebugWebsocketWrite(parse.getClientSn())));
            } else {
                protocol.response(JSON.toJSONString(HttpResult.Fail("未找到对应的处理器["+type.get()+"]")));
            }
        } else {
            protocol.response(JSON.toJSONString(HttpResult.Fail("未指定调试类型[type]")));
        }
    }

    @Override
    public void onClose(DefaultWebSocketServerProtocol protocol) {

    }

    @Override
    public void onBinary(DefaultWebSocketServerProtocol protocol) {

    }

    /**
     * 注册设备编号
     * @param head
     * @return
     */
    @Override
    public Message.MessageHead register(Message.MessageHead head, RegisterParams params) {
        HttpRequestWrapper value = params.getValue(CoreConst.WEBSOCKET_REQ);
        value.getQueryParam("clientSn").ifPresent(clientSn -> head.setEquipCode(clientSn));
        return head;
    }

    /**
     * 不做认证
     * @param request
     * @return
     */
    @Override
    public HttpResponseStatus authentication(HttpRequest request) {
        return HttpResponseStatus.OK;
    }
}
