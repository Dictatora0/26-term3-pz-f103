package me.x1ao.jetlinks.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.x1ao.jetlinks.cfg.JetLinksProperties;
import me.x1ao.jetlinks.event.DeviceFunctionInvokeEvent;
import me.x1ao.jetlinks.event.DevicePropertyWriteEvent;
import me.x1ao.jetlinks.msg.DeviceInvokeFunctionMessage;
import me.x1ao.jetlinks.msg.DeviceWritePropertyMessage;
import org.springframework.context.ApplicationEventPublisher;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

@ClientEndpoint
@Slf4j
public class JetLinksWebSocketClient {

    private final ApplicationEventPublisher eventPublisher;

    private final JetLinksProperties properties;
    private Session session;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public JetLinksWebSocketClient(JetLinksProperties properties, ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    public void connect() throws Exception {
        String uriStr = String.format("ws://%s:%s/%s/%s/socket?token=%s",
                properties.getHost(),
                properties.getPort(),
                properties.getProductId(),
                properties.getDeviceId(),
                properties.getToken()
        );

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, new URI(uriStr));
        connected.set(true);
        log.info("已连接至：" + uriStr);
    }

    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connected.set(false);
        log.info("已断开连接");
    }

    public boolean isConnected() {
        return connected.get() && session != null && session.isOpen();
    }

    public void sendMessage(String message) throws IOException {
        if (!isConnected()) throw new IllegalStateException("WebSocket未连接");
        session.getBasicRemote().sendText(message);
        log.info("发送消息: " + message);
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket 连接已打开");
        this.session = session;
    }


    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("连接关闭: " + reason);
        connected.set(false);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket 发生错误: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("收到平台消息: " + message);
        parseAndPublishEvent(message);
    }

    /**
     * 解析 JSON 消息并发布对应的事件
     */
    private void parseAndPublishEvent(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);
            String messageType = jsonNode.get("messageType").asText();

            switch (messageType) {
                case "INVOKE_FUNCTION":
                    DeviceInvokeFunctionMessage invokeMessage = mapper.treeToValue(jsonNode, DeviceInvokeFunctionMessage.class);
                    eventPublisher.publishEvent(new DeviceFunctionInvokeEvent(this, invokeMessage));

                    sendMessage(mapper.writeValueAsString(invokeMessage));
                    break;
                case "WRITE_PROPERTY":
                    DeviceWritePropertyMessage propertyMessage = mapper.treeToValue(jsonNode, DeviceWritePropertyMessage.class);
                    eventPublisher.publishEvent(new DevicePropertyWriteEvent(this, propertyMessage));
                    sendMessage(mapper.writeValueAsString(propertyMessage));
                    break;
                default:
                    log.warn("未知的消息类型: {}", messageType);
            }
        } catch (Exception e) {
            log.error("解析消息失败: " + message, e);
        }
    }
//
//    @OnMessage
//    public void onMessage(String message) throws IOException {
//        log.info("收到平台消息: " + message);
//        // TODO: 处理平台下发的指令或心跳
//        sendMessage("{\n" +
//                "    \"messageType\":\"WRITE_PROPERTY_REPLY\",\n" +
//                "    \"deviceId\":\"device_iboot\",\n" +
//                "    \"messageId\":\"1930199619448455168\",\n" +
//                "    \"properties\": {\n" +
//                "        \"temp\": 123\n" +
//                "    },\n" +
//                "    \"output\":true\n" +
//                "}");
//    }

}
