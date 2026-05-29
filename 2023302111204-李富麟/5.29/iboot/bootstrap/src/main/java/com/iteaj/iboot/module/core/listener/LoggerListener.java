package com.iteaj.iboot.module.core.listener;

import com.iteaj.framework.Entity;
import com.iteaj.framework.logger.AccessLogger;
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

    @Override
    public void onApplicationEvent(PayloadEvent<AccessLogger> event) {
        AccessLogger payload = event.getPayload();
        Optional<Entity> source = (Optional<Entity>)event.getSource();

        AccessLog accessLog = new AccessLog();
        BeanUtils.copyProperties(payload, accessLog);

        accessLog.setTitle(payload.getProfile());
        source.ifPresent(item -> {
            Admin admin = (Admin) item;
            accessLog.setUserId((Long) item.getId())
                    .setUserName(admin.getName())
                    .setCreateTime(new Date());
        });

        accessLogService.save(accessLog);
    }
}
