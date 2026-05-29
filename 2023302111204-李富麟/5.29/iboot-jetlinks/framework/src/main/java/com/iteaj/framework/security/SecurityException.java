package com.iteaj.framework.security;

import com.iteaj.framework.exception.FrameworkException;

public class SecurityException extends FrameworkException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
