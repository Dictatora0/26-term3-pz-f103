package com.iteaj.iboot.module.iot.utils;

import com.iteaj.framework.logger.LoggerManager;
import com.iteaj.framework.spi.iot.DeviceLogger;
import com.iteaj.iboot.module.iot.IotLoggerFilter;
import com.iteaj.iboot.module.iot.collect.model.DataLoggerFilter;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import com.iteaj.iboot.module.iot.entity.EventSource;

public class IotLogger {

    public static final String NAME = "设备日志";
    public static final String REPORT_NAME = "数据上报";
    public static final String SOURCE_NAME = "数据采集";

    static {
        LoggerManager.addFilter(new IotLoggerFilter());
        LoggerManager.addFilter(new DataLoggerFilter());
    }

    public static void debug(String type, String subType, String uid, String msg, Object... args) {
        DeviceLogger.debug(type, subType, uid, msg, args);
    }

    public static void info(String type, String subType, String uid, String msg, Object... args) {
        DeviceLogger.info(type, subType, uid, msg, args);
    }

    public static void warn(String type, String subType, String uid, String msg, Object... args) {
        DeviceLogger.warn(type, subType, uid, msg, args);
    }

    public static void error(String type, String subType, String uid, String msg, Object... args) {
        DeviceLogger.error(type, subType, uid, msg, args);
    }

    public static void debug(DeviceStatus status, String uid, String msg, Object... args) {
        DeviceLogger.debug(NAME, status.name(), uid, msg, args);
    }

    public static void info(DeviceStatus status, String uid, String msg, Object... args) {
        DeviceLogger.info(NAME, status.name(), uid, msg, args);
    }

    public static void warn(DeviceStatus status, String uid, String msg, Object... args) {
        DeviceLogger.warn(NAME, status.name(), uid, msg, args);
    }

    public static void error(DeviceStatus status, String uid, String msg, Object... args) {
        DeviceLogger.error(NAME, status.name(), uid, msg, args);
    }

    public static void debug(EventSource source, String uid, String msg, Object... args) {
        DeviceLogger.debug(SOURCE_NAME, source.getName(), uid, msg, args);
    }

    public static void info(EventSource source, String uid, String msg, Object... args) {
        DeviceLogger.info(SOURCE_NAME, source.getName(), uid, msg, args);
    }

    public static void warn(EventSource source, String uid, String msg, Object... args) {
        DeviceLogger.warn(SOURCE_NAME, source.getName(), uid, msg, args);
    }

    public static void error(EventSource source, String uid, String msg, Object... args) {
        DeviceLogger.error(SOURCE_NAME, source.getName(), uid, msg, args);
    }
}
