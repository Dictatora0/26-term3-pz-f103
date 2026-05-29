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

import java.util.Date;

/**
 * 监听用户在线状态
 */
public class OnlineUserListener implements FrameworkListener<PayloadEvent<OnlinePayload>>, InitializingBean {

    @Autowired
    private IOnlineUserService service;
    @Autowired
    private FrameworkProperties properties;

    @Async
    @Override
    public void onApplicationEvent(PayloadEvent<OnlinePayload> event) {
        final OnlinePayload payload = event.getPayload();
        OnlineStatus type = payload.getType();
        OnlineUser onlineUser = new OnlineUser(payload.getSessionId());

        if(type == OnlineStatus.Online) { // 用户上线
            final Admin admin = (Admin) payload.getUser();
            final UserAgent agent = payload.getUserAgent();
            final String browse = agent.getBrowser().getName();
            final String os = agent.getOperatingSystem().getName();
            final DeviceType deviceType = agent.getOperatingSystem().getDeviceType();
            final ClientType clientType = ClientType.valueOf(deviceType.name());
            onlineUser.setOs(os)
                    .setLoginTime(new Date())
                    .setStatus(type)
                    .setBrowse(browse)
                    .setType(clientType)
                    .setUserNick(admin.getName())
                    .setAccount(admin.getAccount())
                    .setAccessIp(payload.getAccessIp())
                    .setExpireTime(payload.getExpireTime());
            service.getOne(Wrappers.<OnlineUser>lambdaQuery().eq(OnlineUser::getSessionId, onlineUser.getSessionId()))
                    .ifPresent(user -> {
                user.setLoginTime(onlineUser.getLoginTime()).setStatus(OnlineStatus.Online);
                service.updateById(user);
            }).ifNotPresent(user -> {
                service.save(onlineUser);
            });

        } else if(type == OnlineStatus.Offline) { // 用户下线
            service.updateBySessionId(onlineUser.setStatus(type).setUpdateTime(new Date()));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 如果不是集群则更新所有的用户为离线
        if(!properties.isCluster()) {
            service.update(Wrappers.<OnlineUser>lambdaUpdate()
                    .set(OnlineUser::getStatus, OnlineStatus.Offline)
                    .eq(OnlineUser::getStatus, OnlineStatus.Online));
        }
    }
}
