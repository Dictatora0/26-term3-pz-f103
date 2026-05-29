package com.iteaj.framework.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iteaj.framework.logger.PushAppender.MARKER;
import static com.iteaj.framework.logger.PushAppender.LOGGER;

public class PushLogger {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER);

    public static void debug(String type, String subType, String msg, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(MARKER, "[{}][{}] ==> " + msg, buildArgs(type, subType, args));
        }
    }

    public static void info(String type, String subType, String msg, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(MARKER, "[{}][{}] ==> " + msg, buildArgs(type, subType, args));
        }
    }

    public static void warn(String type, String subType, String msg, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(MARKER, "[{}][{}] ==> " + msg, buildArgs(type, subType, args));
        }
    }

    public static void error(String type, String subType, String msg, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(MARKER, "[{}][{}] ==> " + msg, buildArgs(type, subType, args));
        }
    }

    public static Object[] buildArgs(String type, String subType, Object[] args) {
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = type; newArgs[1] = subType;
        System.arraycopy(args, 0, newArgs, 2, args.length);
        return newArgs;
    }

}
