package com.iteaj.iboot.module.iot.collect;

public class CollectException extends RuntimeException {

    public CollectException(String message) {
        super(message);
    }

    public CollectException(String message, Throwable cause) {
        super(message, cause);
    }
}
