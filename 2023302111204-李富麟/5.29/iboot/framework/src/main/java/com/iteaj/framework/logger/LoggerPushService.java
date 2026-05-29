package com.iteaj.framework.logger;

import org.slf4j.event.Level;

public interface LoggerPushService {

    void push(Level level, PushParams params);
}
