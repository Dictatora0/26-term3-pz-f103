package com.iteaj.framework.spi.message;

import com.alibaba.fastjson.JSONObject;
import com.iteaj.framework.ParamMeta;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface MessageService {

    String DEFAULT_CHANNEL = "DEFAULT";

    String DEFAULT_CHANNEL_NAME = "默认";

    /**
     * 消息类型
     * @return
     */
    String getType();

    /**
     * 消息名称
     * @return
     */
    String getName();

    /**
     * 获取通道标识
     * @return
     */
    default String getChannelId() {
        return DEFAULT_CHANNEL;
    }

    /**
     * 唯一id
     * @return
     */
    default String getConfigId() {
        return this.getType() + ":" + this.getChannelId();
    }

    /**
     * 获取通道名称
     * @return
     */
    default String getChannelName() {
        return DEFAULT_CHANNEL_NAME;
    }

    /**
     * 返回配置参数
     * @see MessageConfig#getConfig()
     * @return
     */
    List<ParamMeta> getConfigParams();

    /**
     * 发送参数
     * @param model
     */
    Optional<Object> send(SendModel model);

    /**
     * 异步发送
     * @param model
     * @param callback
     */
    void sendAsync(SendModel model, Consumer callback);

    /**
     * 移除消息配置
     */
    boolean remove();

    /**
     * 构建服务
     * @param config
     * @return
     */
    MessageService build(JSONObject config);
}
