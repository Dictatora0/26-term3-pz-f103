package com.iteaj.framework.exception;

public class InvokeTimeoutException extends ServiceException{

    public InvokeTimeoutException(String message) {
        super(message);
    }

    public InvokeTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
