package com.iteaj.iboot.plugin.satoken.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.SaLoginModel;
import com.iteaj.framework.Entity;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.admin.event.OnlinePayload;
import com.iteaj.framework.spi.admin.event.OnlineStatus;
import com.iteaj.framework.spi.event.EventUtils;
import com.iteaj.framework.spi.event.PayloadEvent;
import com.iteaj.framework.web.WebUtils;
import eu.bitwalker.useragentutils.UserAgent;

import java.util.Date;

public class SaTokenOnlineListener extends SaTokenListenerForSimple {

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        try {
            final UserAgent agent = SecurityUtil.getAgent();
            final String ipAddress = WebUtils.getIpAddress(SecurityUtil.getRequest());

            final OnlinePayload onlinePayload = new OnlinePayload(OnlineStatus.Online, tokenValue)
                    .setUserAgent(agent).setAccessIp(ipAddress)
                    .setAccessTime(new Date())
                    .setUser((Entity) loginModel.getTokenSignTag())
                    .setExpireTime(loginModel.getTimeout());

            EventUtils.publish(new PayloadEvent(tokenValue, onlinePayload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        // 发布session 停止事件
        EventUtils.publish(new PayloadEvent(tokenValue
                , new OnlinePayload(OnlineStatus.Offline, tokenValue)
                .setAccessTime(new Date())));
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        // 发布session 停止事件
        EventUtils.publish(new PayloadEvent(tokenValue
                , new OnlinePayload(OnlineStatus.Offline, tokenValue)
                .setAccessTime(new Date())));
    }
}
