package com.iteaj.iboot.module.core.config.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.spi.admin.event.OnlinePayload;
import com.iteaj.framework.spi.admin.event.OnlineStatus;
import com.iteaj.framework.spi.event.FrameworkListener;
import com.iteaj.framework.spi.event.PayloadEvent;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.entity.OnlineUser;
import com.iteaj.iboot.module.core.enums.ClientType;
import com.iteaj.iboot.module.core.service.IOnlineUserService;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 监听用户在线状态变化并同步到 online_user 表。
 */
public class OnlineUserListener implements FrameworkListener<PayloadEvent<OnlinePayload>>, InitializingBean {

    @Autowired
    private IOnlineUserService service;
    @Autowired
    private FrameworkProperties properties;

    @Async
    @Override
    public void onApplicationEvent(PayloadEvent<OnlinePayload> event) {
        if (event == null) {
            return;
        }

        final OnlinePayload payload = event.getPayload();
        if (payload == null || payload.getType() == null || !StringUtils.hasText(payload.getSessionId())) {
            return;
        }

        OnlineStatus type = payload.getType();
        OnlineUser onlineUser = new OnlineUser(payload.getSessionId());

        if (type == OnlineStatus.Online) {
            final Admin admin = payload.getUser() instanceof Admin ? (Admin) payload.getUser() : null;
            final UserAgent agent = payload.getUserAgent();
            final String browse = agent != null && agent.getBrowser() != null
                    ? agent.getBrowser().getName() : "Unknown";
            final String os = agent != null && agent.getOperatingSystem() != null
                    ? agent.getOperatingSystem().getName() : "Unknown";
            final DeviceType deviceType = agent != null && agent.getOperatingSystem() != null
                    ? agent.getOperatingSystem().getDeviceType() : null;

            onlineUser.setOs(os)
                    .setLoginTime(new Date())
                    .setStatus(type)
                    .setBrowse(browse)
                    .setType(resolveClientType(deviceType))
                    .setUserNick(admin != null && StringUtils.hasText(admin.getName()) ? admin.getName() : "unknown")
                    .setAccount(admin != null && StringUtils.hasText(admin.getAccount()) ? admin.getAccount() : "unknown")
                    .setAccessIp(payload.getAccessIp())
                    .setExpireTime(payload.getExpireTime());

            service.getOne(Wrappers.<OnlineUser>lambdaQuery()
                            .eq(OnlineUser::getSessionId, onlineUser.getSessionId()))
                    .ifPresent(user -> {
                        user.setLoginTime(onlineUser.getLoginTime()).setStatus(OnlineStatus.Online);
                        service.updateById(user);
                    }).ifNotPresent(user -> service.save(onlineUser));

        } else if (type == OnlineStatus.Offline) {
            service.updateBySessionId(onlineUser.setStatus(type).setUpdateTime(new Date()));
        }
    }

    private ClientType resolveClientType(DeviceType deviceType) {
        if (deviceType == null) {
            return ClientType.UNKNOWN;
        }

        try {
            return ClientType.valueOf(deviceType.name());
        } catch (IllegalArgumentException ex) {
            return ClientType.UNKNOWN;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!properties.isCluster()) {
            service.update(Wrappers.<OnlineUser>lambdaUpdate()
                    .set(OnlineUser::getStatus, OnlineStatus.Offline)
                    .eq(OnlineUser::getStatus, OnlineStatus.Online));
        }
    }
}
