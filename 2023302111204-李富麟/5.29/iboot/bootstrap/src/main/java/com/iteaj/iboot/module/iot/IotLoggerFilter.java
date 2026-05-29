package com.iteaj.iboot.module.iot;

import com.iteaj.framework.logger.LoggerFilter;
import com.iteaj.framework.logger.PushParams;
import com.iteaj.iboot.module.iot.utils.IotLogger;

public class IotLoggerFilter implements LoggerFilter {

    public static final String TYPE = "IOT";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String name() {
        return IotLogger.NAME;
    }

    @Override
    public boolean filter(PushParams params) {
        return params.getType().equals(IotLogger.NAME);
    }
}
