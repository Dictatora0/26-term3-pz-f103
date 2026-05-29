package com.iteaj.framework.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PushAppender extends AppenderBase<ILoggingEvent> {

    public static final String LOGGER = "PUSH:LOGGER";

    public static final Marker MARKER = MarkerFactory.getMarker(LOGGER);

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        Marker marker = iLoggingEvent.getMarker();
        if(marker == MARKER) {
            Object[] args = iLoggingEvent.getArgumentArray();
            // 日志级别
            Level level = converLevel(iLoggingEvent.getLevel());
            // 线程
            String threadName = Thread.currentThread().getName();
            // 异常栈信息
            List<String> stacktrace = getStacktrace(iLoggingEvent);
            // 构建参数
            PushParams params = new PushParams((String) args[0], (String) args[1], iLoggingEvent.getFormattedMessage()
                    , level != null ? level.toString() : "", threadName, Long.toString(iLoggingEvent.getTimeStamp()), stacktrace);
            // 推送
            LoggerManager.push(level, params);
        }

    }

    @NotNull
    private static List<String> getStacktrace(ILoggingEvent iLoggingEvent) {
        List<String> stacktrace = new ArrayList<>();
        if(iLoggingEvent.getThrowableProxy() != null) {
            stacktrace.add(iLoggingEvent.getThrowableProxy().getClassName() + " : " + iLoggingEvent.getThrowableProxy().getMessage());
            StackTraceElementProxy[] proxyArray = iLoggingEvent.getThrowableProxy().getStackTraceElementProxyArray();
            if(proxyArray != null) {
                int length = Math.min(proxyArray.length, 10);
                for (int i = 0; i < length; i++) {
                    stacktrace.add(proxyArray[i].getSTEAsString());
                }
            }
        }

        return stacktrace;
    }

    private Level converLevel(ch.qos.logback.classic.Level level) {
        int levelInt = level.levelInt;
        switch (levelInt) {
            case ch.qos.logback.classic.Level.TRACE_INT: return Level.TRACE;
            case ch.qos.logback.classic.Level.DEBUG_INT: return Level.DEBUG;
            case ch.qos.logback.classic.Level.INFO_INT: return Level.INFO;
            case ch.qos.logback.classic.Level.WARN_INT: return Level.WARN;
            case ch.qos.logback.classic.Level.ERROR_INT: return Level.ERROR;
        }

        return null;
    }

}
