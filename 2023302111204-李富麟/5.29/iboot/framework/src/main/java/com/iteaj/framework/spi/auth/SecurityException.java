package com.iteaj.framework.spi.auth;

import com.iteaj.framework.exception.FrameworkException;

/**
 * create time: 2021/3/27
 *
 * @author iteaj
 * @since 1.0
 */
public class SecurityException extends FrameworkException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }

    public SecurityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
