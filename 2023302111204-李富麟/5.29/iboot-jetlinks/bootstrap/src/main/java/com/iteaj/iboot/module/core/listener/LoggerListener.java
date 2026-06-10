package com.iteaj.iboot.module.core.listener;

import com.iteaj.framework.Entity;
import com.iteaj.framework.logger.AccessLogger;
import com.iteaj.framework.security.JwtPrincipal;
import com.iteaj.framework.spi.event.FrameworkListener;
import com.iteaj.framework.spi.event.PayloadEvent;
import com.iteaj.iboot.module.core.entity.AccessLog;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.service.IAccessLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Optional;

public class LoggerListener implements FrameworkListener<PayloadEvent<AccessLogger>> {

    @Autowired
    private IAccessLogService accessLogService;

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(PayloadEvent<AccessLogger> event) {
        if (event == null || event.getPayload() == null) {
            return;
        }

        AccessLogger payload = event.getPayload();
        Optional<Entity> source = event.getSource() instanceof Optional
                ? (Optional<Entity>) event.getSource()
                : Optional.empty();

        AccessLog accessLog = new AccessLog();
        BeanUtils.copyProperties(payload, accessLog);
        accessLog.setTitle(payload.getProfile());

        source.ifPresent(item -> {
            if (item.getId() != null) {
                accessLog.setUserId(Long.valueOf(String.valueOf(item.getId())));
            }

            if (item instanceof Admin) {
                Admin admin = (Admin) item;
                accessLog.setUserName(admin.getName());
            } else if (item instanceof JwtPrincipal) {
                JwtPrincipal principal = (JwtPrincipal) item;
                accessLog.setUserName(principal.getName());
            }

            accessLog.setCreateTime(new Date());
        });

        accessLogService.save(accessLog);
    }
}
