package com.iteaj.framework.security;

import com.iteaj.framework.exception.FrameworkException;

public class SecurityException extends FrameworkException {

    private final int statusCode;

    public SecurityException(String message) {
        this(message, 500, null);
    }

    public SecurityException(String message, Throwable cause) {
        this(message, 500, cause);
    }

    public SecurityException(String message, int statusCode) {
        this(message, statusCode, null);
    }

    public SecurityException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static SecurityException unauthorized(String message) {
        return new SecurityException(message, 401);
    }

    public static SecurityException forbidden(String message) {
        return new SecurityException(message, 403);
    }
}
