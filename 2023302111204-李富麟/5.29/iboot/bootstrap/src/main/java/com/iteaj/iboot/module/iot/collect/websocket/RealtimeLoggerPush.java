package com.iteaj.iboot.module.iot.collect.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.logger.LoggerManager;
import com.iteaj.framework.logger.LoggerPushService;
import com.iteaj.framework.logger.PushParams;
import com.iteaj.iot.server.websocket.WebSocketChannelMatcher;
import com.iteaj.iot.server.websocket.WebSocketServerListener;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerComponent;
import com.iteaj.iot.server.websocket.impl.DefaultWebSocketServerProtocol;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.util.AttributeKey;
import org.slf4j.event.Level;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class RealtimeLoggerPush implements WebSocketServerListener, LoggerPushService, InitializingBean {

    AttributeKey<JSONObject> PARAM_KEY = AttributeKey.valueOf("logger:params");
    private static final String URI = "/ws/logger/realtime";
    private final DefaultWebSocketServerComponent server;
    private ChannelGroup channelGroup;
    public RealtimeLoggerPush(DefaultWebSocketServerComponent server) {
        this.server = server;
    }

    @Override
    public String uri() {
        return URI;
    }

    @Override
    public void onText(DefaultWebSocketServerProtocol protocol) {
        Channel channel = server.getDeviceManager().find(protocol.getEquipCode());
        if(channel != null) {
            JSONObject params = JSONObject.parseObject(protocol.readText());
            if(!params.containsKey("level") || !params.containsKey("filter")) {
                DefaultWebSocketServerProtocol.close(protocol.getEquipCode()
                        , WebSocketCloseStatus.INVALID_PAYLOAD_DATA, "未指定参数");
                return;
            }

            channel.attr(PARAM_KEY).set(params);
            if(channelGroup == null) {
                channelGroup = this.server.group(URI).get();
            }
        }
    }

    @Override
    public void push(Level level, PushParams params) {
        if(channelGroup != null && !channelGroup.isEmpty()) {
            DefaultWebSocketServerProtocol.writeGroup(URI, JSON.toJSONString(params)
                    , new WebSocketChannelMatcher((channel, session) -> {
                        JSONObject reqParams = channel.attr(PARAM_KEY).get();
                        Integer reqLevel = reqParams.getInteger("level");

                        if(reqLevel != null) {
                            if(level.toInt() < reqLevel) {
                                return false;
                            }
                        }

                        JSONArray filter = reqParams.getJSONArray("filter");
                        if(filter == null || filter.isEmpty()) {
                            return true;
                        }

                        long count = filter.stream()
                                .map(type -> LoggerManager.getFilter((String) type))
                                .filter(item -> item.filter(params))
                                .count();

                        return count > 0;
                    })
            );
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LoggerManager.addPushService(this);
    }
}
