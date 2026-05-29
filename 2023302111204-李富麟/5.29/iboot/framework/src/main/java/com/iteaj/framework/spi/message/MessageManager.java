package com.iteaj.framework.spi.message;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息服务管理
 */
public class MessageManager {

    private Map<String, MessageService> serviceMap = new HashMap<>(16);

    public MessageManager(List<MessageService> services) {
        if(CollectionUtil.isNotEmpty(services)) {
            services.forEach(item -> this.register(item));
        }
    }

    /**
     * 注册服务
     * @param service
     * @return 已经存在返回 {@code null} 否则返回注册的对象
     */
    public MessageService register(MessageService service) {
        if(service == null) {
            throw new IllegalArgumentException("参数[service]不能为空");
        }

        if(StrUtil.isBlank(service.getType()) || StrUtil.isBlank(service.getChannelId())) {
            throw new IllegalArgumentException("参数[type] or [channelId]不能为空");
        }

        String key = service.getConfigId();
        if(serviceMap.containsKey(key)) {
            return null;
        } else {
            return serviceMap.put(key, service);
        }
    }

    /**
     * 注册或者更新为默认
     * @param service
     * @return
     */
    public MessageService registerOrUpdateDefault(MessageService service) {
        if(service == null) {
            throw new IllegalArgumentException("参数[service]不能为空");
        }

        if(StrUtil.isBlank(service.getType())) {
            throw new IllegalArgumentException("参数[type]不能为空");
        }

        String key = getKey(service.getType(), MessageService.DEFAULT_CHANNEL);
        return serviceMap.put(key, service);
    }

    /**
     * 是否存在指定服务
     * @param type 消息类型
     * @param channelId 通道标识
     * @return
     */
    public boolean contains(String type, String channelId) {
        return serviceMap.containsKey(getKey(type, channelId));
    }

    /**
     * 获取指定类型的默认通道服务
     * @param type
     * @return
     */
    public MessageService getDefault(String type) {
        return this.serviceMap.get(getKey(type, MessageService.DEFAULT_CHANNEL));
    }

    /**
     * 获取指定服务
     * @param type
     * @param channelId
     * @return
     */
    public MessageService getService(String type, String channelId) {
        return serviceMap.get(getKey(type, channelId));
    }

    /**
     * 移除某个服务
     * @param type
     * @param channelId
     * @return
     */
    public MessageService remove(String type, String channelId) {
        MessageService remove = serviceMap.remove(getKey(type, channelId));
        if(remove != null) {
            remove.remove();
        }

        return remove;
    }

    public String getKey(String type, String channelId) {
        return type + ":" + channelId;
    }

    /**
     * 获取类型列表
     * @return
     */
    public List<String> getTypes() {
        return this.serviceMap.values()
                .stream()
                .map(item -> item.getType())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 返回指定类型下的所有服务
     * @param type
     * @return
     */
    public Set<MessageService> getServices(String type) {
        return this.serviceMap.entrySet().stream()
                .filter(item -> item.getValue().getType().equals(type))
                .map(item -> item.getValue())
                .collect(Collectors.toSet());
    }

    /**
     * 消息服务列表
     * @return
     */
    public Collection<MessageService> getServices() {
        return Collections.unmodifiableCollection(this.serviceMap.values());
    }
}
